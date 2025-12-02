import { json } from '@sveltejs/kit';
import { createVenta, getVentas } from '$lib/stock.js';

export async function GET({ url }) {
  try {
    const limit = parseInt(url.searchParams.get('limit')) || 50;
    const ventas = getVentas(limit);
    return json(ventas);
  } catch (error) {
    console.error('Error al obtener ventas:', error);
    return json({ error: 'Error al obtener ventas' }, { status: 500 });
  }
}

export async function POST({ request, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const data = await request.json();
    const { productos, clienteNombre, subtotal, total } = data;

    if (!productos || productos.length === 0) {
      return json({ error: 'Debe incluir al menos un producto' }, { status: 400 });
    }

    if (!clienteNombre) {
      return json({ error: 'El nombre del cliente es obligatorio' }, { status: 400 });
    }

    const resultado = createVenta(data, locals.user.id);
    return json(resultado);
  } catch (error) {
    console.error('Error al crear venta:', error);
    return json({ error: error.message || 'Error al crear venta' }, { status: 500 });
  }
}