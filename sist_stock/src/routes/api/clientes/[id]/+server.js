import { json } from '@sveltejs/kit';
import { getCliente, updateCliente, deleteCliente } from '$lib/stock.js';

export async function GET({ params }) {
  try {
    const cliente = getCliente(params.id);
    if (!cliente) {
      return json({ error: 'Cliente no encontrado' }, { status: 404 });
    }
    return json(cliente);
  } catch (error) {
    console.error('Error al obtener cliente:', error);
    return json({ error: 'Error al obtener cliente' }, { status: 500 });
  }
}

export async function PUT({ request, params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    const data = await request.json();
    const { nombre, documento, telefono, email, direccion } = data;

    if (!nombre) {
      return json({ error: 'El nombre es obligatorio' }, { status: 400 });
    }

    const actualizado = updateCliente(params.id, data);

    if (!actualizado) {
      return json({ error: 'Cliente no encontrado' }, { status: 404 });
    }

    return json({ success: true });
  } catch (error) {
    console.error('Error al actualizar cliente:', error);
    return json({ error: error.message || 'Error al actualizar cliente' }, { status: 500 });
  }
}

export async function DELETE({ params, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    // Solo administradores pueden eliminar clientes
    if (locals.user.rol !== 'administrador') {
      return json({ error: 'Permisos insuficientes' }, { status: 403 });
    }

    const eliminado = deleteCliente(params.id);
    
    if (!eliminado) {
      return json({ error: 'Cliente no encontrado' }, { status: 404 });
    }

    return json({ success: true });
  } catch (error) {
    console.error('Error al eliminar cliente:', error);
    return json({ error: error.message || 'Error al eliminar cliente' }, { status: 500 });
  }
}