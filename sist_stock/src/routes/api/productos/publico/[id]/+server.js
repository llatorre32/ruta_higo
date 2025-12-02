import { json } from '@sveltejs/kit';
import { getProductoPublico } from '$lib/stock.js';

export async function GET({ params }) {
  try {
    const producto = getProductoPublico(params.id);
    
    if (!producto) {
      return json({ 
        error: 'Producto no encontrado o no disponible' 
      }, { status: 404 });
    }
    
    return json(producto);
  } catch (error) {
    console.error('Error al obtener producto público:', error);
    return json({ 
      error: 'Error al obtener producto' 
    }, { status: 500 });
  }
}