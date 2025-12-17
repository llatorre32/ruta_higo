import { getVentas } from '$lib/stock.js';

export async function load({ depends }) {
  depends('app:ventas');
  
  try {
    const ventas = getVentas(50);
    return {
      ventas
    };
  } catch (error) {
    console.error('Error al cargar ventas:', error);
    return {
      ventas: []
    };
  }
}