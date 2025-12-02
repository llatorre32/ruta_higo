import { getResumenStock, getProductosBajoStock } from '$lib/stock.js';

export function load() {
  try {
    const resumen = getResumenStock();
    const productosBajoStock = getProductosBajoStock();
    
    return {
      resumen,
      productosBajoStock
    };
  } catch (error) {
    console.error('Error al cargar datos del dashboard:', error);
    return {
      resumen: {
        totalProductos: 0,
        productosBajoStock: 0,
        valorInventario: 0,
        totalCategorias: 0
      },
      productosBajoStock: []
    };
  }
}