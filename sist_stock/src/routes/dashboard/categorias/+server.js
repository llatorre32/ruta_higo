import { createCategoria, updateCategoria, deleteCategoria } from '$lib/stock.js';
import { json } from '@sveltejs/kit';

export async function POST({ request, locals }) {
  if (!locals.user) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }

  try {
    const { nombre, descripcion } = await request.json();
    
    if (!nombre) {
      return json({ error: 'El nombre es obligatorio' }, { status: 400 });
    }
    
    const categoria = createCategoria(nombre, descripcion || '', locals.user.id);
    return json({ success: true, categoria });
    
  } catch (error) {
    console.error('Error al crear categoría:', error);
    return json({ error: error.message || 'Error al crear la categoría' }, { status: 500 });
  }
}

export async function PUT({ request, locals }) {
  if (!locals.user) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }

  try {
    const { id, nombre, descripcion } = await request.json();
    
    if (!id || !nombre) {
      return json({ error: 'ID y nombre son obligatorios' }, { status: 400 });
    }
    
    const success = updateCategoria(id, { nombre, descripcion: descripcion || '' });
    
    if (success) {
      return json({ success: true });
    } else {
      return json({ error: 'Categoría no encontrada' }, { status: 404 });
    }
    
  } catch (error) {
    console.error('Error al actualizar categoría:', error);
    return json({ error: error.message || 'Error al actualizar la categoría' }, { status: 500 });
  }
}

export async function DELETE({ request, locals }) {
  if (!locals.user || locals.user.rol !== 'administrador') {
    return json({ error: 'No autorizado' }, { status: 401 });
  }

  try {
    const { id } = await request.json();
    
    if (!id) {
      return json({ error: 'ID de la categoría es requerido' }, { status: 400 });
    }
    
    const success = deleteCategoria(id);
    
    if (success) {
      return json({ success: true });
    } else {
      return json({ error: 'Categoría no encontrada' }, { status: 404 });
    }
    
  } catch (error) {
    console.error('Error al eliminar categoría:', error);
    return json({ error: error.message || 'Error al eliminar la categoría' }, { status: 500 });
  }
}