import { registrarMovimientoStock } from '$lib/stock.js';
import { json } from '@sveltejs/kit';

export async function POST({ request, locals }) {
  if (!locals.user) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }

  try {
    const { productoId, tipo, cantidad, motivo } = await request.json();
    
    if (!productoId || !tipo || !cantidad) {
      return json({ error: 'Faltan campos obligatorios' }, { status: 400 });
    }
    
    if (!['entrada', 'salida', 'ajuste'].includes(tipo)) {
      return json({ error: 'Tipo de movimiento inválido' }, { status: 400 });
    }
    
    if (parseInt(cantidad) <= 0 && tipo !== 'ajuste') {
      return json({ error: 'La cantidad debe ser mayor a 0' }, { status: 400 });
    }
    
    if (tipo === 'ajuste' && parseInt(cantidad) < 0) {
      return json({ error: 'El stock no puede ser negativo' }, { status: 400 });
    }
    
    const resultado = registrarMovimientoStock(
      productoId,
      tipo,
      parseInt(cantidad),
      motivo || '',
      locals.user.id
    );
    
    return json({ 
      success: true, 
      movimiento: resultado,
      mensaje: `Stock ${tipo === 'entrada' ? 'agregado' : tipo === 'salida' ? 'reducido' : 'ajustado'} correctamente`
    });
    
  } catch (error) {
    console.error('Error al registrar movimiento:', error);
    return json({ error: error.message || 'Error al registrar el movimiento' }, { status: 500 });
  }
}