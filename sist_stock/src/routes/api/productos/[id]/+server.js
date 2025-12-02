import { json } from '@sveltejs/kit';
import { getProducto, updateProducto, deleteProducto } from '$lib/stock.js';

export async function GET({ params }) {
  try {
    const producto = getProducto(params.id);
    if (!producto) {
      return json({ error: 'Producto no encontrado' }, { status: 404 });
    }
    return json(producto);
  } catch (error) {
    console.error('Error al obtener producto:', error);
    return json({ error: 'Error al obtener producto' }, { status: 500 });
  }
}

import { json } from '@sveltejs/kit';
import { getProducto, updateProducto, deleteProducto } from '$lib/stock.js';
import { writeFileSync, unlinkSync } from 'fs';
import { join } from 'path';
import { v4 as uuidv4 } from 'uuid';

export async function GET({ params }) {
  try {
    const producto = getProducto(params.id);
    if (!producto) {
      return json({ error: 'Producto no encontrado' }, { status: 404 });
    }
    return json(producto);
  } catch (error) {
    console.error('Error al obtener producto:', error);
    return json({ error: 'Error al obtener producto' }, { status: 500 });
  }
}

export async function PUT({ request, params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const contentType = request.headers.get('content-type');
    let data;
    let imagenPath = null;

    if (contentType && contentType.includes('multipart/form-data')) {
      // Manejar FormData con imagen
      const formData = await request.formData();
      data = {
        nombre: formData.get('nombre'),
        descripcion: formData.get('descripcion'),
        codigo: formData.get('codigo'),
        categoriaId: formData.get('categoriaId'),
        precioCompra: formData.get('precioCompra'),
        precioVenta: formData.get('precioVenta'),
        stockMinimo: formData.get('stockMinimo'),
        stockMaximo: formData.get('stockMaximo')
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
        const productoActual = getProducto(params.id);
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

    const { nombre, descripcion, codigo, categoriaId, precioCompra, precioVenta, stockMinimo, stockMaximo } = data;

    if (!nombre || !codigo || !categoriaId) {
      return json({ error: 'Faltan campos obligatorios' }, { status: 400 });
    }

    const updateData = {
      nombre,
      descripcion,
      codigo,
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

    const actualizado = updateProducto(params.id, updateData);

    if (!actualizado) {
      return json({ error: 'Producto no encontrado' }, { status: 404 });
    }

    return json({ success: true });
  } catch (error) {
    console.error('Error al actualizar producto:', error);
    return json({ error: error.message || 'Error al actualizar producto' }, { status: 500 });
  }
}

export async function DELETE({ params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    // Solo administradores pueden eliminar productos
    if (locals.user.rol !== 'administrador') {
      return json({ error: 'Permisos insuficientes' }, { status: 403 });
    }

    const eliminado = deleteProducto(params.id);
    
    if (!eliminado) {
      return json({ error: 'Producto no encontrado' }, { status: 404 });
    }

    return json({ success: true });
  } catch (error) {
    console.error('Error al eliminar producto:', error);
    return json({ error: error.message || 'Error al eliminar producto' }, { status: 500 });
  }
}