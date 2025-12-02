import { json } from '@sveltejs/kit';
import { createCliente, getClientes } from '$lib/stock.js';

export async function GET() {
  try {
    const clientes = getClientes();
    return json(clientes);
  } catch (error) {
    console.error('Error al obtener clientes:', error);
    return json({ error: 'Error al obtener clientes' }, { status: 500 });
  }
}

export async function POST({ request, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const data = await request.json();
    const { nombre, documento, telefono, email, direccion } = data;

    if (!nombre) {
      return json({ error: 'El nombre es obligatorio' }, { status: 400 });
    }

    const resultado = createCliente(data, locals.user.id);
    return json(resultado);
  } catch (error) {
    console.error('Error al crear cliente:', error);
    return json({ error: error.message || 'Error al crear cliente' }, { status: 500 });
  }
}