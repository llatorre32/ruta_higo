import { Link } from 'react-router-dom';
import bannerImg from '../assets/img/banner.jpg';
import presentacionImg from '../assets/img/presentacion.jpg';
import higo1 from '../assets/img/productos/higo1.jpg';
import plantin1 from '../assets/img/productos/plantin1.jpg';
import higo2 from '../assets/img/productos/higo2.jpg';
import './Home.css';

const Home = () => {
  return (
    <div className="home">
      {/* Banner principal */}
      <section 
        className="banner" 
        style={{ backgroundImage: `url(${bannerImg})` }}
      >
        <div className="banner-texto">
          <h1>Ruta del Higo Catamarca</h1>
          <p>Descubrí el sabor artesanal de nuestros dulces y productos naturales.</p>
          <Link to="/productos" className="btn">Ver productos</Link>
        </div>
      </section>

      {/* Sección presentación */}
      <section className="presentacion">
        <div className="container">
          <div className="texto">
            <h2>🍯 Un dulce camino hacia el sabor</h2>
            <p>
              En <strong>Ruta del Higo</strong> recorremos un camino donde la tierra, el sol y la tradición catamarqueña se unen 
              para dar vida a productos únicos. Cultivamos nuestros higuerales con respeto por los procesos naturales, 
              cuidando cada detalle desde el plantín hasta el frasco final.
            </p>
            <p>
              Elaboramos <strong>dulces artesanales, plantines y una variedad de productos derivados del higo</strong>, 
              manteniendo intacta la esencia de lo natural y el sabor auténtico de nuestra tierra. Cada preparación nace del trabajo familiar, 
              del compromiso con la calidad y del amor por lo que hacemos.
            </p>
            <p>
              En cada frasco encontrarás más que un producto: <em>una historia de esfuerzo, de raíces y de identidad catamarqueña</em>. 
              Un homenaje a quienes, generación tras generación, han sabido transformar el fruto del higo en una verdadera delicia que conquista los sentidos.
            </p>
            <p>
              <strong>Ruta del Higo</strong> es mucho más que una marca: es una invitación a descubrir el sabor, la tradición 
              y la dulzura de nuestra tierra.
            </p>
          </div>
          <img src={presentacionImg} alt="Dulces artesanales de higo" className="imagen" />
        </div>
      </section>

      {/* Sección productos destacados */}
      <section className="productos">
        <div className="container">
          <h2>Productos destacados</h2>
          <div className="grid">
            <div className="item">
              <img src={higo1} alt="Dulce de higo artesanal" />
              <h3>Dulce de Higo</h3>
              <p>Frascos de 500g elaborados con fruta seleccionada.</p>
            </div>
            <div className="item">
              <img src={plantin1} alt="Plantines de higo" />
              <div className="item-content">
                <h3>Plantines de Higuera</h3>
                <p>Ideal para tu huerta o jardín. Variedades locales.</p>
              </div>
            </div>
            <div className="item">
              <img src={higo2} alt="Higos frescos" />
              <div className="item-content">
                <h3>Higos frescos</h3>
                <p>Higos frescos, listos para disfrutar.</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;