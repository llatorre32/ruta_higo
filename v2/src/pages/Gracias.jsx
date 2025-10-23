import { Link } from 'react-router-dom';
import logo from '../assets/img/logos/logo.png';
import './Gracias.css';

const Gracias = () => {
  return (
    <div className="gracias-page">
      {/* Sección de agradecimiento */}
      <section className="gracias">
        <div className="container">
          <img src={logo} alt="Ruta del Higo Catamarca" className="logo-gracias" />
          <h1>¡Gracias por contactarte con nosotros!</h1>
          <p>Tu mensaje fue enviado correctamente. En breve nos comunicaremos con vos.</p>
          <Link to="/" className="btn">Volver al inicio</Link>
        </div>
      </section>
    </div>
  );
};

export default Gracias;