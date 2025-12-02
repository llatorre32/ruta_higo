import { getProductos, getCategorias } from '$lib/stock.js';

export function load() {
  try {
    const productos = getProductos();
    const categorias = getCategorias();
    
    return {
      productos,
      categorias
    };
  } catch (error) {
    console.error('Error al cargar productos:', error);
    return {
      productos: [],
      categorias: []
    };
  }
}