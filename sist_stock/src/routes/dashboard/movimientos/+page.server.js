import { getMovimientosStock, getProductos } from '$lib/stock.js';

export function load({ depends }) {
  depends('app:movimientos');
  depends('app:productos');
  
  try {
    const movimientos = getMovimientosStock();
    const productos = getProductos();
    
    return {
      movimientos,
      productos
    };
  } catch (error) {
    console.error('Error al cargar movimientos:', error);
    return {
      movimientos: [],
      productos: []
    };
  }
}