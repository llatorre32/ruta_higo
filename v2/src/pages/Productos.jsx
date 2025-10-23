import higo1 from '../assets/img/productos/higo1.jpg';
import higo2 from '../assets/img/productos/higo2.jpg';
import plantin1 from '../assets/img/productos/plantin1.jpg';
import mermelada from '../assets/img/productos/mermelada.jpg';
import './Productos.css';

const Productos = () => {
  const productos = [
    {
      id: 1,
      imagen: higo1,
      titulo: "Dulce de Higo Artesanal",
      descripcion: "Frascos de 500g elaborados con fruta seleccionada y azúcar orgánica.",
      precio: "$2.500",
      accion: "Realizar pedido"
    },
    {
      id: 2,
      imagen: higo2,
      titulo: "Higos frescos de temporada",
      descripcion: "Higos frescos recién cosechados, listos para disfrutar.",
      precio: "$2.800",
      accion: "Realizar pedido"
    },
    {
      id: 3,
      imagen: plantin1,
      titulo: "Plantines de Higuera",
      descripcion: "Variedades locales, listas para trasplante y cultivo.",
      precio: "$1.200",
      accion: "Consultar disponibilidad"
    },
    {
      id: 4,
      imagen: mermelada,
      titulo: "Mermelada de Higo con Nuez",
      descripcion: "Combinación perfecta entre el sabor dulce del higo y la textura de la nuez.",
      precio: "$3.000",
      accion: "Realizar pedido"
    }
  ];

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

          <div className="catalogo-grid">
            {productos.map((producto) => (
              <div key={producto.id} className="card">
                <img src={producto.imagen} alt={producto.titulo} />
                <div className="card-content">
                  <h3>{producto.titulo}</h3>
                  <p>{producto.descripcion}</p>
                  <span className="precio">{producto.precio}</span>
                  <button className="btn">{producto.accion}</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
};

export default Productos;