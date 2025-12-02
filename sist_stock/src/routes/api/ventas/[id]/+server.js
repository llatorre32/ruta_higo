import { json } from '@sveltejs/kit';
import { getVenta, cancelarVenta } from '$lib/stock.js';

export async function GET({ params }) {
  try {
    const venta = getVenta(params.id);
    if (!venta) {
      return json({ error: 'Venta no encontrada' }, { status: 404 });
    }
    return json(venta);
  } catch (error) {
    console.error('Error al obtener venta:', error);
    return json({ error: 'Error al obtener venta' }, { status: 500 });
  }
}

export async function DELETE({ params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    // Solo administradores pueden cancelar ventas
    if (locals.user.rol !== 'administrador') {
      return json({ error: 'Permisos insuficientes' }, { status: 403 });
    }

    const cancelada = cancelarVenta(params.id, locals.user.id);
    
    if (!cancelada) {
      return json({ error: 'Venta no encontrada' }, { status: 404 });
    }

    return json({ success: true });
  } catch (error) {
    console.error('Error al cancelar venta:', error);
    return json({ error: error.message || 'Error al cancelar venta' }, { status: 500 });
  }
}