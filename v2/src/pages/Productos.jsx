import { useState, useEffect } from 'react';
import defaultImage from '../assets/img/productos/higo1.jpg';
import './Productos.css';

const Productos = () => {
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProductos = async () => {
      try {
        setLoading(true);
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/productos/publico?limit=10`);
        
        if (!response.ok) {
          throw new Error('Error al cargar los productos');
        }
        
        const data = await response.json();
        setProductos(data.productos || []);
      } catch (err) {
        setError(err.message);
        console.error('Error fetching productos:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProductos();
  }, []);

  return (
    <div className="productos-page">
      {/* Sección principal del catálogo */}
      <section className="catalogo">
        <div className="container">
          <h1>Catálogo de Productos</h1>
          <p className="intro">
            Conocé nuestros productos elaborados artesanalmente con higos seleccionados de Catamarca.  
            Dulces, almíbares y plantines listos para llevarte un pedacito de nuestra tierra.
          </p>

          {loading && (
            <div className="loading">
              <p>Cargando productos...</p>
            </div>
          )}

          {error && (
            <div className="error">
              <p>Error: {error}</p>
            </div>
          )}

          {!loading && !error && productos.length === 0 && (
            <div className="no-productos">
              <p>No hay productos disponibles en este momento.</p>
            </div>
          )}

          {!loading && !error && productos.length > 0 && (
            <div className="catalogo-grid">
              {productos.map((producto) => (
                <div key={producto.id} className="card">
                  <img 
                    src={producto.imagen ? `${import.meta.env.VITE_API_BASE_URL.replace('/api', '')}${producto.imagen}` : defaultImage} 
                    alt={producto.nombre}
                    onError={(e) => {
                      e.target.src = defaultImage;
                    }}
                  />
                  <div className="card-content">
                    <h3>{producto.nombre}</h3>
                    <p>{producto.descripcion}</p>
                    <span className="precio">${(producto.precio_venta / 100).toLocaleString('es-AR', { minimumFractionDigits: 2 })}</span>
                    <p className="codigo">Código: {producto.codigo}</p>
                    <p className="stock">Stock: {producto.stock_actual} unidades</p>                    
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
};

export default Productos;