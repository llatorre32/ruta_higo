import { json } from '@sveltejs/kit';
import { getReporte, ejecutarReporte } from '$lib/stock.js';

export async function GET({ params }) {
  try {
    const reporte = getReporte(params.id);
    if (!reporte) {
      return json({ error: 'Reporte no encontrado' }, { status: 404 });
    }
    return json(reporte);
  } catch (error) {
    console.error('Error al obtener reporte:', error);
    return json({ error: 'Error al obtener reporte' }, { status: 500 });
  }
}

export async function POST({ request, params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const data = await request.json();
    const parametros = data.parametros || {};

    const resultado = ejecutarReporte(params.id, parametros);
    return json(resultado);
  } catch (error) {
    console.error('Error al ejecutar reporte:', error);
    return json({ error: error.message || 'Error al ejecutar reporte' }, { status: 500 });
  }
}