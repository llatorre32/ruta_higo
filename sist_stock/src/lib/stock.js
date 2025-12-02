import { getDatabase } from './database.js';
import { v4 as uuidv4 } from 'uuid';

// ========== CATEGORÍAS ==========

export function createCategoria(nombre, descripcion) {
  const db = getDatabase();
  const id = uuidv4();
  
  const result = db.prepare(`
    INSERT INTO categorias (id, nombre, descripcion)
    VALUES (?, ?, ?)
  `).run(id, nombre, descripcion);
  
  return result.changes > 0 ? { id, nombre, descripcion } : null;
}

export function getCategorias() {
  const db = getDatabase();
  return db.prepare('SELECT * FROM categorias WHERE activo = 1 ORDER BY nombre').all();
}

export function getCategoria(id) {
  const db = getDatabase();
  return db.prepare('SELECT * FROM categorias WHERE id = ? AND activo = 1').get(id);
}

export function updateCategoria(id, data) {
  const db = getDatabase();
  const { nombre, descripcion } = data;
  const result = db.prepare(`
    UPDATE categorias 
    SET nombre = ?, descripcion = ?, updated_at = CURRENT_TIMESTAMP 
    WHERE id = ? AND activo = 1
  `).run(nombre, descripcion, id);
  
  return result.changes > 0;
}

export function deleteCategoria(id) {
  const db = getDatabase();
  // Verificar que no tenga productos asociados
  const productCount = db.prepare('SELECT COUNT(*) as count FROM productos WHERE categoria_id = ? AND activo = 1').get(id);
  
  if (productCount.count > 0) {
    throw new Error('No se puede eliminar la categoría porque tiene productos asociados');
  }
  
  const result = db.prepare(`
    UPDATE categorias 
    SET activo = 0, updated_at = CURRENT_TIMESTAMP 
    WHERE id = ?
  `).run(id);
  
  return result.changes > 0;
}

// ========== PRODUCTOS ==========

export function createProducto(data) {
  const db = getDatabase();
  const id = uuidv4();
  const { 
    codigo, nombre, descripcion, categoriaId, 
    precioCompra = 0, precioVenta = 0, 
    stockMinimo = 0, stockMaximo = 0, 
    stockActual = 0, usuarioId, imagen 
  } = data;
  
  const transaction = db.transaction(() => {
    // Crear producto
    const result = db.prepare(`
      INSERT INTO productos (id, codigo, nombre, descripcion, categoria_id, 
                           precio_compra, precio_venta, stock_minimo, stock_maximo, stock_actual, imagen)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(id, codigo, nombre, descripcion, categoriaId, 
           precioCompra, precioVenta, stockMinimo, stockMaximo, stockActual, imagen);
    
    if (result.changes === 0) {
      throw new Error('Error al crear el producto');
    }
    
    // Si hay stock inicial, crear movimiento
    if (stockActual > 0 && usuarioId) {
      const movimientoId = uuidv4();
      db.prepare(`
        INSERT INTO movimientos_stock (id, producto_id, tipo, cantidad, motivo, usuario_id)
        VALUES (?, ?, ?, ?, ?, ?)
      `).run(movimientoId, id, 'entrada', stockActual, 'Stock inicial', usuarioId);
    }
    
    return { id, codigo, nombre, descripcion, categoriaId, precioCompra, precioVenta, stockMinimo, stockMaximo, stockActual, imagen };
  });
  
  return transaction();
}

export function getProductos() {
  const db = getDatabase();
  return db.prepare(`
    SELECT p.*, c.nombre as categoria_nombre
    FROM productos p
    LEFT JOIN categorias c ON p.categoria_id = c.id
    WHERE p.activo = 1
    ORDER BY p.nombre
  `).all();
}

export function getProducto(id) {
  const db = getDatabase();
  return db.prepare(`
    SELECT p.*, c.nombre as categoria_nombre
    FROM productos p
    LEFT JOIN categorias c ON p.categoria_id = c.id
    WHERE p.id = ? AND p.activo = 1
  `).get(id);
}

export function updateProducto(id, data) {
  const db = getDatabase();
  const { 
    codigo, nombre, descripcion, categoriaId, 
    precioCompra, precioVenta, stockMinimo, stockMaximo, imagen 
  } = data;
  
  const result = db.prepare(`
    UPDATE productos 
    SET codigo = ?, nombre = ?, descripcion = ?, categoria_id = ?, 
        precio_compra = ?, precio_venta = ?, stock_minimo = ?, stock_maximo = ?, imagen = ?, 
        updated_at = CURRENT_TIMESTAMP 
    WHERE id = ? AND activo = 1
  `).run(codigo, nombre, descripcion, categoriaId, precioCompra, precioVenta, 
         stockMinimo, stockMaximo, imagen, id);
  
  return result.changes > 0;
}

export function deleteProducto(id) {
  const db = getDatabase();
  const result = db.prepare(`
    UPDATE productos 
    SET activo = 0, updated_at = CURRENT_TIMESTAMP 
    WHERE id = ?
  `).run(id);
  
  return result.changes > 0;
}

// ========== BÚSQUEDA PÚBLICA DE PRODUCTOS ==========

export function buscarProductosPublico(termino = '', limit = 50) {
  const db = getDatabase();
  
  if (!termino.trim()) {
    // Si no hay término de búsqueda, devolver todos los productos activos
    return db.prepare(`
      SELECT id, codigo, nombre, descripcion, stock_actual, precio_venta, imagen
      FROM productos 
      WHERE activo = 1
      ORDER BY nombre
      LIMIT ?
    `).all(limit);
  }
  
  // Búsqueda por nombre, código o descripción
  const terminoBusqueda = `%${termino.trim().toLowerCase()}%`;
  return db.prepare(`
    SELECT id, codigo, nombre, descripcion, stock_actual, precio_venta, imagen
    FROM productos 
    WHERE activo = 1 
      AND (LOWER(nombre) LIKE ? 
           OR LOWER(codigo) LIKE ? 
           OR LOWER(descripcion) LIKE ?)
    ORDER BY 
      CASE 
        WHEN LOWER(nombre) LIKE ? THEN 1
        WHEN LOWER(codigo) LIKE ? THEN 2
        ELSE 3
      END,
      nombre
    LIMIT ?
  `).all(terminoBusqueda, terminoBusqueda, terminoBusqueda, 
         `%${termino.trim().toLowerCase()}%`, `%${termino.trim().toLowerCase()}%`, limit);
}

export function getProductoPublico(id) {
  const db = getDatabase();
  return db.prepare(`
    SELECT id, codigo, nombre, descripcion, stock_actual, precio_venta, imagen
    FROM productos 
    WHERE id = ? AND activo = 1
  `).get(id);
}

// ========== MOVIMIENTOS DE STOCK ==========

export function registrarMovimientoStock(productoId, tipo, cantidad, motivo, usuarioId) {
  const db = getDatabase();
  const id = uuidv4();
  
  const transaction = db.transaction(() => {
    // Obtener stock actual
    const producto = db.prepare('SELECT stock_actual FROM productos WHERE id = ? AND activo = 1').get(productoId);
    if (!producto) {
      throw new Error('Producto no encontrado');
    }
    
    let nuevoStock = producto.stock_actual;
    
    if (tipo === 'entrada') {
      nuevoStock += cantidad;
    } else if (tipo === 'salida') {
      if (cantidad > producto.stock_actual) {
        throw new Error('No hay suficiente stock disponible');
      }
      nuevoStock -= cantidad;
    } else if (tipo === 'ajuste') {
      nuevoStock = cantidad;
    }
    
    // Registrar movimiento
    db.prepare(`
      INSERT INTO movimientos_stock (id, producto_id, tipo, cantidad, motivo, usuario_id)
      VALUES (?, ?, ?, ?, ?, ?)
    `).run(id, productoId, tipo, cantidad, motivo, usuarioId);
    
    // Actualizar stock del producto
    db.prepare('UPDATE productos SET stock_actual = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?')
      .run(nuevoStock, productoId);
    
    return { id, stockAnterior: producto.stock_actual, stockNuevo: nuevoStock };
  });
  
  return transaction();
}

export function getMovimientosStock(productoId = null, limit = 50) {
  const db = getDatabase();
  let query = `
    SELECT m.*, p.nombre as producto_nombre, p.codigo as producto_codigo, u.nombre as usuario_nombre
    FROM movimientos_stock m
    JOIN productos p ON m.producto_id = p.id
    JOIN usuarios u ON m.usuario_id = u.id
  `;
  
  let params = [];
  if (productoId) {
    query += ' WHERE m.producto_id = ?';
    params.push(productoId);
  }
  
  query += ' ORDER BY m.created_at DESC LIMIT ?';
  params.push(limit);
  
  return db.prepare(query).all(...params);
}

// ========== REPORTES ==========

export function getProductosBajoStock() {
  const db = getDatabase();
  return db.prepare(`
    SELECT p.*, c.nombre as categoria_nombre
    FROM productos p
    LEFT JOIN categorias c ON p.categoria_id = c.id
    WHERE p.stock_actual <= p.stock_minimo AND p.activo = 1
    ORDER BY (p.stock_actual - p.stock_minimo) ASC
  `).all();
}

export function getResumenStock() {
  const db = getDatabase();
  
  const totalProductos = db.prepare('SELECT COUNT(*) as count FROM productos WHERE activo = 1').get().count;
  const productosBajoStock = db.prepare('SELECT COUNT(*) as count FROM productos WHERE stock_actual <= stock_minimo AND activo = 1').get().count;
  const valorInventario = db.prepare('SELECT SUM(stock_actual * precio_compra) as valor FROM productos WHERE activo = 1').get().valor || 0;
  const totalCategorias = db.prepare('SELECT COUNT(*) as count FROM categorias WHERE activo = 1').get().count;
  
  return {
    totalProductos,
    productosBajoStock,
    valorInventario: Math.round(valorInventario * 100) / 100,
    totalCategorias
  };
}

// ========== CLIENTES ==========

export function createCliente(data, usuarioId) {
  const db = getDatabase();
  const id = uuidv4();
  const { nombre, documento, telefono, email, direccion } = data;
  
  const result = db.prepare(`
    INSERT INTO clientes (id, nombre, documento, telefono, email, direccion)
    VALUES (?, ?, ?, ?, ?, ?)
  `).run(id, nombre, documento, telefono, email, direccion);
  
  return result.changes > 0 ? { id, ...data } : null;
}

export function getClientes() {
  const db = getDatabase();
  return db.prepare('SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre').all();
}

export function getCliente(id) {
  const db = getDatabase();
  return db.prepare('SELECT * FROM clientes WHERE id = ? AND activo = 1').get(id);
}

export function updateCliente(id, data) {
  const db = getDatabase();
  const { nombre, documento, telefono, email, direccion } = data;
  
  const result = db.prepare(`
    UPDATE clientes 
    SET nombre = ?, documento = ?, telefono = ?, email = ?, direccion = ?, updated_at = CURRENT_TIMESTAMP 
    WHERE id = ? AND activo = 1
  `).run(nombre, documento, telefono, email, direccion, id);
  
  return result.changes > 0;
}

export function deleteCliente(id) {
  const db = getDatabase();
  const result = db.prepare(`
    UPDATE clientes 
    SET activo = 0, updated_at = CURRENT_TIMESTAMP 
    WHERE id = ?
  `).run(id);
  
  return result.changes > 0;
}

// ========== VENTAS ==========

export function createVenta(data, usuarioId) {
  const db = getDatabase();
  const id = uuidv4();
  
  const transaction = db.transaction(() => {
    const { 
      clienteNombre, clienteDocumento, clienteTelefono, clienteEmail, 
      productos, subtotal, descuento, impuestos, total, metodoPago 
    } = data;
    
    // Generar número de venta
    const numeroVenta = `V${Date.now()}`;
    
    // Crear la venta
    const ventaResult = db.prepare(`
      INSERT INTO ventas (id, numero_venta, cliente_nombre, cliente_documento, 
                         cliente_telefono, cliente_email, subtotal, descuento, 
                         impuestos, total, metodo_pago, usuario_id)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `).run(id, numeroVenta, clienteNombre, clienteDocumento, clienteTelefono, 
           clienteEmail, subtotal, descuento, impuestos, total, metodoPago, usuarioId);
    
    // Agregar detalles de la venta
    const insertDetalle = db.prepare(`
      INSERT INTO ventas_detalle (id, venta_id, producto_id, cantidad, precio_unitario, subtotal)
      VALUES (?, ?, ?, ?, ?, ?)
    `);
    
    const updateStock = db.prepare(`
      UPDATE productos SET stock_actual = stock_actual - ?, updated_at = CURRENT_TIMESTAMP 
      WHERE id = ?
    `);
    
    for (const producto of productos) {
      const detalleId = uuidv4();
      const subtotalDetalle = producto.cantidad * producto.precioUnitario;
      
      // Verificar stock disponible
      const stockActual = db.prepare('SELECT stock_actual FROM productos WHERE id = ?').get(producto.id);
      if (!stockActual || stockActual.stock_actual < producto.cantidad) {
        throw new Error(`Stock insuficiente para el producto ${producto.nombre}`);
      }
      
      // Insertar detalle
      insertDetalle.run(detalleId, id, producto.id, producto.cantidad, 
                       producto.precioUnitario, subtotalDetalle);
      
      // Actualizar stock
      updateStock.run(producto.cantidad, producto.id);
      
      // Registrar movimiento de stock (se hace fuera de la transacción)
      const movimientoId = uuidv4();
      db.prepare(`
        INSERT INTO movimientos_stock (id, producto_id, tipo, cantidad, motivo, usuario_id)
        VALUES (?, ?, ?, ?, ?, ?)
      `).run(movimientoId, producto.id, 'salida', producto.cantidad, 
             `Venta ${numeroVenta}`, usuarioId);
    }
    
    return {
      id, 
      numero_venta: numeroVenta,
      cliente_nombre: clienteNombre,
      cliente_documento: clienteDocumento,
      cliente_telefono: clienteTelefono,
      cliente_email: clienteEmail,
      subtotal,
      descuento,
      impuestos,
      total,
      metodo_pago: metodoPago,
      estado: 'completada'
    };
  });
  
  return transaction();
}

export function getVentas(limit = 50) {
  const db = getDatabase();
  return db.prepare(`
    SELECT v.*, u.nombre as usuario_nombre
    FROM ventas v
    JOIN usuarios u ON v.usuario_id = u.id
    ORDER BY v.created_at DESC
    LIMIT ?
  `).all(limit);
}

export function getVenta(id) {
  const db = getDatabase();
  const venta = db.prepare(`
    SELECT v.*, u.nombre as usuario_nombre
    FROM ventas v
    JOIN usuarios u ON v.usuario_id = u.id
    WHERE v.id = ?
  `).get(id);
  
  if (!venta) return null;
  
  const detalles = db.prepare(`
    SELECT vd.*, p.nombre as producto_nombre, p.codigo as producto_codigo
    FROM ventas_detalle vd
    JOIN productos p ON vd.producto_id = p.id
    WHERE vd.venta_id = ?
  `).all(id);
  
  return { ...venta, detalles };
}

export function cancelarVenta(id, usuarioId) {
  const db = getDatabase();
  
  const transaction = db.transaction(() => {
    // Obtener detalles de la venta
    const venta = getVenta(id);
    if (!venta) {
      throw new Error('Venta no encontrada');
    }
    
    if (venta.estado === 'cancelada') {
      throw new Error('La venta ya está cancelada');
    }
    
    // Cancelar la venta
    db.prepare(`
      UPDATE ventas 
      SET estado = 'cancelada', updated_at = CURRENT_TIMESTAMP 
      WHERE id = ?
    `).run(id);
    
    // Revertir stock
    for (const detalle of venta.detalles) {
      db.prepare(`
        UPDATE productos SET stock_actual = stock_actual + ?, updated_at = CURRENT_TIMESTAMP 
        WHERE id = ?
      `).run(detalle.cantidad, detalle.producto_id);
      
      // Registrar movimiento de devolución
      const movimientoId = uuidv4();
      db.prepare(`
        INSERT INTO movimientos_stock (id, producto_id, tipo, cantidad, motivo, usuario_id)
        VALUES (?, ?, ?, ?, ?, ?)
      `).run(movimientoId, detalle.producto_id, 'entrada', detalle.cantidad, 
             `Devolución venta ${venta.numero_venta}`, usuarioId);
    }
    
    return true;
  });
  
  return transaction();
}

// ========== REPORTES ==========

export function createReporte(data, usuarioId) {
  const db = getDatabase();
  const id = uuidv4();
  const { nombre, descripcion, tipo, query, parametros } = data;
  
  const result = db.prepare(`
    INSERT INTO reportes (id, nombre, descripcion, tipo, query, parametros, created_by)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run(id, nombre, descripcion, tipo, query, JSON.stringify(parametros), usuarioId);
  
  return result.changes > 0 ? { id, ...data } : null;
}

export function getReportes() {
  const db = getDatabase();
  return db.prepare(`
    SELECT r.*, u.nombre as creador_nombre
    FROM reportes r
    JOIN usuarios u ON r.created_by = u.id
    WHERE r.activo = 1
    ORDER BY r.nombre
  `).all();
}

export function getReporte(id) {
  const db = getDatabase();
  return db.prepare(`
    SELECT r.*, u.nombre as creador_nombre
    FROM reportes r
    JOIN usuarios u ON r.created_by = u.id
    WHERE r.id = ? AND r.activo = 1
  `).get(id);
}

export function ejecutarReporte(id, parametros = {}) {
  const db = getDatabase();
  const reporte = getReporte(id);
  
  if (!reporte) {
    throw new Error('Reporte no encontrado');
  }
  
  // Aquí puedes implementar la lógica para reemplazar parámetros en la query
  let query = reporte.query;
  
  // Ejecutar la query
  try {
    const resultados = db.prepare(query).all();
    return { reporte, resultados };
  } catch (error) {
    throw new Error(`Error ejecutando reporte: ${error.message}`);
  }
}

export function getReportesVentas(fechaInicio, fechaFin) {
  const db = getDatabase();
  
  const ventasPorDia = db.prepare(`
    SELECT DATE(created_at) as fecha, COUNT(*) as cantidad_ventas, SUM(total) as total_ventas
    FROM ventas 
    WHERE estado = 'completada' AND DATE(created_at) BETWEEN ? AND ?
    GROUP BY DATE(created_at)
    ORDER BY fecha DESC
  `).all(fechaInicio, fechaFin);
  
  const productosMasVendidos = db.prepare(`
    SELECT p.nombre, p.codigo, SUM(vd.cantidad) as total_vendido, SUM(vd.subtotal) as total_ingresos
    FROM ventas_detalle vd
    JOIN productos p ON vd.producto_id = p.id
    JOIN ventas v ON vd.venta_id = v.id
    WHERE v.estado = 'completada' AND DATE(v.created_at) BETWEEN ? AND ?
    GROUP BY p.id
    ORDER BY total_vendido DESC
    LIMIT 10
  `).all(fechaInicio, fechaFin);
  
  const ventasPorMetodoPago = db.prepare(`
    SELECT metodo_pago, COUNT(*) as cantidad, SUM(total) as total
    FROM ventas
    WHERE estado = 'completada' AND DATE(created_at) BETWEEN ? AND ?
    GROUP BY metodo_pago
  `).all(fechaInicio, fechaFin);
  
  return {
    ventasPorDia,
    productosMasVendidos,
    ventasPorMetodoPago
  };
}

// ========== REPORTES DE VENTAS ESPECÍFICOS ==========

export function getReporteVentasMensual(year = null, limit = 12) {
  const db = getDatabase();
  const currentYear = year || new Date().getFullYear();
  
  return db.prepare(`
    SELECT 
      strftime('%Y-%m', created_at) as mes,
      strftime('%Y', created_at) as año,
      CASE strftime('%m', created_at)
        WHEN '01' THEN 'Enero'
        WHEN '02' THEN 'Febrero'  
        WHEN '03' THEN 'Marzo'
        WHEN '04' THEN 'Abril'
        WHEN '05' THEN 'Mayo'
        WHEN '06' THEN 'Junio'
        WHEN '07' THEN 'Julio'
        WHEN '08' THEN 'Agosto'
        WHEN '09' THEN 'Septiembre'
        WHEN '10' THEN 'Octubre'
        WHEN '11' THEN 'Noviembre'
        WHEN '12' THEN 'Diciembre'
      END as mes_nombre,
      COUNT(*) as cantidad_ventas,
      SUM(total) as total_ventas,
      AVG(total) as promedio_venta,
      SUM(subtotal) as total_subtotal,
      SUM(descuento) as total_descuentos,
      SUM(impuestos) as total_impuestos
    FROM ventas 
    WHERE estado = 'completada' AND strftime('%Y', created_at) = ?
    GROUP BY strftime('%Y-%m', created_at)
    ORDER BY mes DESC
    LIMIT ?
  `).all(currentYear.toString(), limit);
}

export function getReporteVentasPorVendedor(fechaInicio = null, fechaFin = null, limit = 20) {
  const db = getDatabase();
  
  // Si no se especifican fechas, usar el mes actual
  if (!fechaInicio || !fechaFin) {
    const now = new Date();
    fechaInicio = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
    fechaFin = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0];
  }
  
  return db.prepare(`
    SELECT 
      u.id as vendedor_id,
      u.nombre as vendedor_nombre,
      u.email as vendedor_email,
      COUNT(*) as cantidad_ventas,
      SUM(v.total) as total_ventas,
      AVG(v.total) as promedio_venta,
      MIN(v.total) as venta_minima,
      MAX(v.total) as venta_maxima,
      SUM(v.subtotal) as total_subtotal,
      SUM(v.descuento) as total_descuentos,
      SUM(v.impuestos) as total_impuestos,
      ROUND(SUM(v.total) * 100.0 / (
        SELECT SUM(total) 
        FROM ventas 
        WHERE estado = 'completada' AND DATE(created_at) BETWEEN ? AND ?
      ), 2) as porcentaje_total
    FROM ventas v
    JOIN usuarios u ON v.usuario_id = u.id
    WHERE v.estado = 'completada' AND DATE(v.created_at) BETWEEN ? AND ?
    GROUP BY u.id, u.nombre, u.email
    ORDER BY total_ventas DESC
    LIMIT ?
  `).all(fechaInicio, fechaFin, fechaInicio, fechaFin, limit);
}

export function getReporteProductosMasVendidos(fechaInicio = null, fechaFin = null, limit = 20) {
  const db = getDatabase();
  
  // Si no se especifican fechas, usar el mes actual
  if (!fechaInicio || !fechaFin) {
    const now = new Date();
    fechaInicio = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
    fechaFin = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0];
  }
  
  return db.prepare(`
    SELECT 
      p.id as producto_id,
      p.codigo as producto_codigo,
      p.nombre as producto_nombre,
      p.precio_venta as precio_actual,
      c.nombre as categoria_nombre,
      SUM(vd.cantidad) as total_vendido,
      COUNT(DISTINCT v.id) as ventas_realizadas,
      SUM(vd.subtotal) as total_ingresos,
      AVG(vd.precio_unitario) as precio_promedio,
      MIN(vd.precio_unitario) as precio_minimo,
      MAX(vd.precio_unitario) as precio_maximo,
      ROUND(SUM(vd.cantidad) * 100.0 / (
        SELECT SUM(cantidad) 
        FROM ventas_detalle vd2
        JOIN ventas v2 ON vd2.venta_id = v2.id
        WHERE v2.estado = 'completada' AND DATE(v2.created_at) BETWEEN ? AND ?
      ), 2) as porcentaje_ventas,
      p.stock_actual
    FROM ventas_detalle vd
    JOIN productos p ON vd.producto_id = p.id
    LEFT JOIN categorias c ON p.categoria_id = c.id
    JOIN ventas v ON vd.venta_id = v.id
    WHERE v.estado = 'completada' AND DATE(v.created_at) BETWEEN ? AND ?
    GROUP BY p.id, p.codigo, p.nombre, p.precio_venta, c.nombre
    ORDER BY total_vendido DESC, total_ingresos DESC
    LIMIT ?
  `).all(fechaInicio, fechaFin, fechaInicio, fechaFin, limit);
}