import { error } from '@sveltejs/kit';

export async function load({ params, fetch }) {
  try {
    const response = await fetch(`/api/ventas/${params.id}`);
    
    if (!response.ok) {
      if (response.status === 404) {
        throw error(404, 'Venta no encontrada');
      }
      throw error(500, 'Error al cargar la venta');
    }
    
    const venta = await response.json();
    
    return {
      venta
    };
  } catch (err) {
    if (err.status) {
      throw err;
    }
    throw error(500, 'Error al cargar la venta');
  }
}