import { json } from '@sveltejs/kit';
import { registrarMovimientoStock, getMovimientosStock } from '$lib/stock.js';

export async function GET({ url }) {
  try {
    const productoId = url.searchParams.get('productoId');
    const limit = parseInt(url.searchParams.get('limit')) || 50;
    
    const movimientos = getMovimientosStock(productoId, limit);
    return json(movimientos);
  } catch (error) {
    console.error('Error al obtener movimientos:', error);
    return json({ error: 'Error al obtener movimientos' }, { status: 500 });
  }
}

export async function POST({ request, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const data = await request.json();
    const { productoId, tipo, cantidad, motivo } = data;

    if (!productoId || !tipo || !cantidad || cantidad <= 0) {
      return json({ error: 'Faltan campos obligatorios o cantidad inválida' }, { status: 400 });
    }

    if (!['entrada', 'salida', 'ajuste'].includes(tipo)) {
      return json({ error: 'Tipo de movimiento inválido' }, { status: 400 });
    }

    const resultado = registrarMovimientoStock(
      productoId, 
      tipo, 
      parseInt(cantidad), 
      motivo || '', 
      locals.user.id
    );

    return json(resultado);
  } catch (error) {
    console.error('Error al registrar movimiento:', error);
    return json({ error: error.message || 'Error al registrar movimiento' }, { status: 500 });
  }
}