import { createProducto, updateProducto, deleteProducto, registrarMovimientoStock, getProducto } from '$lib/stock.js';
import { json } from '@sveltejs/kit';

import { createProducto, updateProducto, deleteProducto, registrarMovimientoStock, getProducto } from '$lib/stock.js';
import { json } from '@sveltejs/kit';
import { writeFileSync, unlinkSync } from 'fs';
import { join } from 'path';
import { v4 as uuidv4 } from 'uuid';

export async function POST({ request, locals }) {
  if (!locals.user) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }

  try {
    const contentType = request.headers.get('content-type');
    let data;
    let imagenPath = null;

    if (contentType && contentType.includes('multipart/form-data')) {
      // Manejar FormData con imagen
      const formData = await request.formData();
      data = {
        codigo: formData.get('codigo'),
        nombre: formData.get('nombre'),
        descripcion: formData.get('descripcion'),
        categoriaId: formData.get('categoriaId'),
        precioCompra: formData.get('precioCompra'),
        precioVenta: formData.get('precioVenta'),
        stockMinimo: formData.get('stockMinimo'),
        stockMaximo: formData.get('stockMaximo'),
        stockInicial: formData.get('stockInicial')
      };

      const imagen = formData.get('imagen');
      
      if (imagen && imagen.size > 0) {
        // Validar tipo de archivo
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(imagen.type)) {
          return json({ error: 'Tipo de archivo no válido. Solo se permiten JPEG, PNG, GIF y WebP' }, { status: 400 });
        }

        // Validar tamaño (máximo 5MB)
        if (imagen.size > 5 * 1024 * 1024) {
          return json({ error: 'El archivo es demasiado grande. Máximo 5MB' }, { status: 400 });
        }

        // Generar nombre único para la imagen
        const extension = imagen.name.split('.').pop();
        const nombreArchivo = `${uuidv4()}.${extension}`;
        const rutaArchivo = join('static/uploads', nombreArchivo);

        try {
          const buffer = Buffer.from(await imagen.arrayBuffer());
          writeFileSync(rutaArchivo, buffer);
          imagenPath = `/uploads/${nombreArchivo}`;
        } catch (error) {
          console.error('Error al guardar imagen:', error);
          return json({ error: 'Error al guardar la imagen' }, { status: 500 });
        }
      }
    } else {
      // Manejar JSON
      data = await request.json();
    }
    
    const { 
      codigo, nombre, descripcion, categoriaId, 
      precioCompra, precioVenta, stockMinimo, stockMaximo, stockInicial 
    } = data;
    
    if (!codigo || !nombre || !categoriaId) {
      return json({ error: 'Faltan campos obligatorios' }, { status: 400 });
    }
    
    const producto = createProducto({
      codigo,
      nombre,
      descripcion: descripcion || '',
      categoriaId,
      precioCompra: parseFloat(precioCompra) || 0,
      precioVenta: parseFloat(precioVenta) || 0,
      stockMinimo: parseInt(stockMinimo) || 0,
      stockMaximo: parseInt(stockMaximo) || 0,
      stockActual: parseInt(stockInicial) || 0,
      imagen: imagenPath,
      usuarioId: locals.user.id
    });
    
    return json({ success: true, producto });
    
  } catch (error) {
    console.error('Error al crear producto:', error);
    return json({ error: error.message || 'Error al crear el producto' }, { status: 500 });
  }
}

export async function PUT({ request, locals }) {
  if (!locals.user) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }

  try {
    const contentType = request.headers.get('content-type');
    let data;
    let imagenPath = null;

    if (contentType && contentType.includes('multipart/form-data')) {
      // Manejar FormData con imagen
      const formData = await request.formData();
      data = {
        id: formData.get('id'),
        codigo: formData.get('codigo'),
        nombre: formData.get('nombre'),
        descripcion: formData.get('descripcion'),
        categoriaId: formData.get('categoriaId'),
        precioCompra: formData.get('precioCompra'),
        precioVenta: formData.get('precioVenta'),
        stockMinimo: formData.get('stockMinimo'),
        stockMaximo: formData.get('stockMaximo'),
        stockAjuste: formData.get('stockAjuste'),
        tipoAjuste: formData.get('tipoAjuste')
      };

      const imagen = formData.get('imagen');
      
      if (imagen && imagen.size > 0) {
        // Validar tipo de archivo
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(imagen.type)) {
          return json({ error: 'Tipo de archivo no válido. Solo se permiten JPEG, PNG, GIF y WebP' }, { status: 400 });
        }

        // Validar tamaño (máximo 5MB)
        if (imagen.size > 5 * 1024 * 1024) {
          return json({ error: 'El archivo es demasiado grande. Máximo 5MB' }, { status: 400 });
        }

        // Obtener producto actual para eliminar imagen anterior si existe
        const productoActual = getProducto(data.id);
        if (productoActual && productoActual.imagen) {
          try {
            const rutaAnterior = join('static', productoActual.imagen);
            unlinkSync(rutaAnterior);
          } catch (error) {
            console.warn('No se pudo eliminar la imagen anterior:', error);
          }
        }

        // Generar nombre único para la nueva imagen
        const extension = imagen.name.split('.').pop();
        const nombreArchivo = `${uuidv4()}.${extension}`;
        const rutaArchivo = join('static/uploads', nombreArchivo);

        try {
          const buffer = Buffer.from(await imagen.arrayBuffer());
          writeFileSync(rutaArchivo, buffer);
          imagenPath = `/uploads/${nombreArchivo}`;
        } catch (error) {
          console.error('Error al guardar imagen:', error);
          return json({ error: 'Error al guardar la imagen' }, { status: 500 });
        }
      }
    } else {
      // Manejar JSON
      data = await request.json();
    }
    
    const { 
      id, codigo, nombre, descripcion, categoriaId, 
      precioCompra, precioVenta, stockMinimo, stockMaximo,
      stockAjuste, tipoAjuste 
    } = data;
    
    if (!id || !codigo || !nombre || !categoriaId) {
      return json({ error: 'Faltan campos obligatorios' }, { status: 400 });
    }
    
    // Preparar datos de actualización
    const updateData = {
      codigo,
      nombre,
      descripcion: descripcion || '',
      categoriaId,
      precioCompra: parseFloat(precioCompra) || 0,
      precioVenta: parseFloat(precioVenta) || 0,
      stockMinimo: parseInt(stockMinimo) || 0,
      stockMaximo: parseInt(stockMaximo) || 0
    };

    // Si hay nueva imagen, agregarla a los datos de actualización
    if (imagenPath !== null) {
      updateData.imagen = imagenPath;
    }
    
    // Actualizar los datos básicos del producto
    const success = updateProducto(id, updateData);
    
    if (!success) {
      return json({ error: 'Producto no encontrado' }, { status: 404 });
    }
    
    // Si hay ajuste de stock, procesarlo
    if (stockAjuste !== undefined && stockAjuste !== 0) {
      const ajusteNumerico = parseInt(stockAjuste) || 0;
      
      if (tipoAjuste === 'reemplazo') {
        // Reemplazar stock actual por el valor especificado
        registrarMovimientoStock(
          id, 
          'ajuste', 
          ajusteNumerico, 
          'Ajuste de stock (reemplazo)', 
          locals.user.id
        );
      } else if (tipoAjuste === 'suma' && ajusteNumerico !== 0) {
        // Sumar o restar del stock actual
        const tipoMovimiento = ajusteNumerico > 0 ? 'entrada' : 'salida';
        const cantidad = Math.abs(ajusteNumerico);
        const motivo = ajusteNumerico > 0 ? 'Ajuste de stock (entrada)' : 'Ajuste de stock (salida)';
        
        registrarMovimientoStock(
          id, 
          tipoMovimiento, 
          cantidad, 
          motivo, 
          locals.user.id
        );
      }
    }
    
    return json({ success: true });
    
  } catch (error) {
    console.error('Error al actualizar producto:', error);
    return json({ error: error.message || 'Error al actualizar el producto' }, { status: 500 });
  }
}

export async function DELETE({ request, locals }) {
  if (!locals.user || locals.user.rol !== 'administrador') {
    return json({ error: 'No autorizado' }, { status: 401 });
  }

  try {
    const { id } = await request.json();
    
    if (!id) {
      return json({ error: 'ID del producto es requerido' }, { status: 400 });
    }
    
    const success = deleteProducto(id);
    
    if (success) {
      return json({ success: true });
    } else {
      return json({ error: 'Producto no encontrado' }, { status: 404 });
    }
    
  } catch (error) {
    console.error('Error al eliminar producto:', error);
    return json({ error: error.message || 'Error al eliminar el producto' }, { status: 500 });
  }
}