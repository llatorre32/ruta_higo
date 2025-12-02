import { json } from '@sveltejs/kit';
import { getCategoria, updateCategoria, deleteCategoria } from '$lib/stock.js';

export async function GET({ params }) {
  try {
    const categoria = getCategoria(params.id);
    if (!categoria) {
      return json({ error: 'Categoría no encontrada' }, { status: 404 });
    }
    return json(categoria);
  } catch (error) {
    console.error('Error al obtener categoría:', error);
    return json({ error: 'Error al obtener categoría' }, { status: 500 });
  }
}

export async function PUT({ request, params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const data = await request.json();
    const { nombre, descripcion } = data;

    if (!nombre) {
      return json({ error: 'El nombre es obligatorio' }, { status: 400 });
    }

    const actualizada = updateCategoria(params.id, { nombre, descripcion });

    if (!actualizada) {
      return json({ error: 'Categoría no encontrada' }, { status: 404 });
    }

    return json({ success: true });
  } catch (error) {
    console.error('Error al actualizar categoría:', error);
    return json({ error: error.message || 'Error al actualizar categoría' }, { status: 500 });
  }
}

export async function DELETE({ params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    // Solo administradores pueden eliminar categorías
    if (locals.user.rol !== 'administrador') {
      return json({ error: 'Permisos insuficientes' }, { status: 403 });
    }

    const eliminada = deleteCategoria(params.id);
    
    if (!eliminada) {
      return json({ error: 'Categoría no encontrada' }, { status: 404 });
    }

    return json({ success: true });
  } catch (error) {
    console.error('Error al eliminar categoría:', error);
    return json({ error: error.message || 'Error al eliminar categoría' }, { status: 500 });
  }
}