import { getCategorias } from '$lib/stock.js';

export function load({ depends }) {
  depends('app:categorias');
  
  try {
    const categorias = getCategorias();
    return { categorias };
  } catch (error) {
    console.error('Error al cargar categorías:', error);
    return { categorias: [] };
  }
}