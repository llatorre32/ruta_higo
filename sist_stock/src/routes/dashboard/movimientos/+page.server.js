import { getMovimientosStock, getProductos } from '$lib/stock.js';

export function load() {
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