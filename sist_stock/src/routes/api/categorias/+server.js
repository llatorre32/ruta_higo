import { json } from '@sveltejs/kit';
import { createCategoria, getCategorias } from '$lib/stock.js';

export async function GET() {
  try {
    const categorias = getCategorias();
    return json(categorias);
  } catch (error) {
    console.error('Error al obtener categorías:', error);
    return json({ error: 'Error al obtener categorías' }, { status: 500 });
  }
}

export async function POST({ request, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const data = await request.json();
    const { nombre, descripcion } = data;

    if (!nombre) {
      return json({ error: 'El nombre es obligatorio' }, { status: 400 });
    }

    const resultado = createCategoria(nombre, descripcion, locals.user.id);
    return json(resultado);
  } catch (error) {
    console.error('Error al crear categoría:', error);
    return json({ error: error.message || 'Error al crear categoría' }, { status: 500 });
  }
}