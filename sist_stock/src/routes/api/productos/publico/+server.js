import { json } from '@sveltejs/kit';
import { buscarProductosPublico, getProductoPublico } from '$lib/stock.js';

export async function GET({ url }) {
  try {
    const termino = url.searchParams.get('q') || '';
    const limit = parseInt(url.searchParams.get('limit')) || 50;
    
    const productos = buscarProductosPublico(termino, limit);
    
    return json({
      productos,
      total: productos.length,
      termino: termino.trim(),
      mensaje: termino.trim() 
        ? `Se encontraron ${productos.length} productos que coinciden con "${termino.trim()}"`
        : `Se muestran ${productos.length} productos disponibles`
    });
  } catch (error) {
    console.error('Error en búsqueda pública de productos:', error);
    return json({ 
      error: 'Error al buscar productos',
      productos: [],
      total: 0 
    }, { status: 500 });
  }
}