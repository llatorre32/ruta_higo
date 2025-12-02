import { json } from '@sveltejs/kit';
import { createProducto, getProductos } from '$lib/stock.js';
import { writeFileSync } from 'fs';
import { join } from 'path';
import { v4 as uuidv4 } from 'uuid';

export async function GET({ url }) {
  try {
    const limit = parseInt(url.searchParams.get('limit')) || 50;
    const productos = getProductos(limit);
    return json(productos);
  } catch (error) {
    console.error('Error al obtener productos:', error);
    return json({ error: 'Error al obtener productos' }, { status: 500 });
  }
}

export async function POST({ request, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const formData = await request.formData();
    
    const nombre = formData.get('nombre');
    const descripcion = formData.get('descripcion');
    const codigo = formData.get('codigo');
    const categoriaId = formData.get('categoriaId');
    const precioCompra = formData.get('precioCompra');
    const precioVenta = formData.get('precioVenta');
    const stockMinimo = formData.get('stockMinimo');
    const stockMaximo = formData.get('stockMaximo');
    const stockActual = formData.get('stockActual');
    const imagen = formData.get('imagen');

    if (!nombre || !codigo || !categoriaId) {
      return json({ error: 'Faltan campos obligatorios' }, { status: 400 });
    }

    let imagenPath = null;

    // Procesar imagen si se subió una
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
        // Convertir archivo a buffer y guardarlo
        const buffer = Buffer.from(await imagen.arrayBuffer());
        writeFileSync(rutaArchivo, buffer);
        imagenPath = `/uploads/${nombreArchivo}`;
      } catch (error) {
        console.error('Error al guardar imagen:', error);
        return json({ error: 'Error al guardar la imagen' }, { status: 500 });
      }
    }

    const resultado = createProducto({
      nombre,
      descripcion,
      codigo,
      categoriaId,
      precioCompra: parseFloat(precioCompra) || 0,
      precioVenta: parseFloat(precioVenta) || 0,
      stockMinimo: parseInt(stockMinimo) || 0,
      stockMaximo: parseInt(stockMaximo) || 0,
      stockActual: parseInt(stockActual) || 0,
      imagen: imagenPath,
      usuarioId: locals.user.id
    });

    return json(resultado);
  } catch (error) {
    console.error('Error al crear producto:', error);
    return json({ error: error.message || 'Error al crear producto' }, { status: 500 });
  }
}