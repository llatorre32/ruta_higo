import { getClientes } from '$lib/stock.js';

export async function load() {
  try {
    const clientes = getClientes();
    return {
      clientes
    };
  } catch (error) {
    console.error('Error al cargar clientes:', error);
    return {
      clientes: []
    };
  }
}