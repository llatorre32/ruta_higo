import { useState } from 'react';
import facebookIcon from '../assets/img/facebook.png';
import instagramIcon from '../assets/img/instagram.png';
import whatsappIcon from '../assets/img/whatsapp.png';
import './Contacto.css';

const Contacto = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    email: '',
    mensaje: ''
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Aquí podrías manejar el envío del formulario
    console.log('Formulario enviado:', formData);
    // Por ahora, simplemente mostramos una alerta
    alert('¡Gracias por contactarte con nosotros! En breve nos comunicaremos contigo.');
    
    // Limpiar el formulario
    setFormData({
      nombre: '',
      email: '',
      mensaje: ''
    });
  };

  return (
    <div className="contacto-page">
      {/* Sección de contacto */}
      <section className="contacto">
        <div className="container">
          <h1>Contacto</h1>
          <p className="intro">
            Si querés realizar un pedido, consultar disponibilidad o conocer más sobre nuestros productos,
            completá el formulario o comunicate por nuestras redes sociales.
          </p>

          <div className="grid-contacto">
            {/* Formulario */}
            <form className="formulario" onSubmit={handleSubmit}>
              <label htmlFor="nombre">Nombre:</label>
              <input 
                type="text" 
                id="nombre" 
                name="nombre" 
                value={formData.nombre}
                onChange={handleChange}
                required 
              />

              <label htmlFor="email">Correo electrónico:</label>
              <input 
                type="email" 
                id="email" 
                name="email" 
                value={formData.email}
                onChange={handleChange}
                required 
              />

              <label htmlFor="mensaje">Mensaje:</label>
              <textarea 
                id="mensaje" 
                name="mensaje" 
                rows="5" 
                value={formData.mensaje}
                onChange={handleChange}
                required
              ></textarea>

              <button type="submit" className="btn">Enviar mensaje</button>
            </form>

            {/* Datos de contacto */}
            <div className="datos">
              <h3>Datos del emprendimiento</h3>
              <p><strong>Ruta del Higo Catamarca</strong></p>
              <p>📍 El Pantanillo, San Fernando del Valle de Catamarca</p>
              <p>📞 0383 430-6994</p>
              <p>📧 higoscatamarca@gmail.com</p>

              <h3>Seguinos</h3>
              <div className="redes-contacto">
                <a href="#" aria-label="Facebook">
                  <img src={facebookIcon} alt="Facebook" />
                </a> 
                <a href="#" aria-label="Instagram">
                  <img src={instagramIcon} alt="Instagram" />
                </a>
                <a href="#" aria-label="WhatsApp">
                  <img src={whatsappIcon} alt="WhatsApp" />
                </a>
              </div>
            </div>
          </div>

          {/* Mapa */}
          <div className="mapa">
            <iframe 
              src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2670.037536051832!2d-65.80332502568002!3d-28.53130106276765!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x9424297ed9062de9%3A0xab676b250c7a9379!2sParque%20Industrial%20El%20Pantanillo!5e1!3m2!1ses!2sar!4v1761092360858!5m2!1ses!2sar"
              width="100%" 
              height="350" 
              style={{border: 0}} 
              allowFullScreen="" 
              loading="lazy"
              title="Ubicación Ruta del Higo Catamarca"
            ></iframe>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Contacto;