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

  const [enviando, setEnviando] = useState(false);
  const [mensaje, setMensaje] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setEnviando(true);
    setMensaje('');
    /*
    {
  "para": "higoscatamarca1@gmail.com",
  "asunto": "Prueba desde Postman",
  "mensaje": "Este es un mensaje de prueba enviado desde Postman.\n\nSaludos!",
  "ventaId": "optional-123"
}
    */
try {
    const data1 = {
      para: "higoscatamarca1@gmail.com",
      asunto: `Consulta de ${formData.nombre} desde la web`,
      ventaId: "optional-123",
      mensaje: `Nombre: ${formData.nombre}
Email: ${formData.email}
Mensaje: ${formData.mensaje}
Enviado desde el formulario web de Ruta del Higo Catamarca`
    };

    
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/email`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data1),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        if (data.simulated) {
          setMensaje('¡Mensaje procesado correctamente! (Modo simulado - Te responderemos a la brevedad)');
        } else {
          setMensaje('¡Mensaje enviado correctamente! Te responderemos a la brevedad.');
        }
        setFormData({ nombre: '', email: '', mensaje: '' });
      } else {
        setMensaje(`Error: ${data.error || 'No se pudo enviar el mensaje'}`);
      }
    } catch (error) {
      console.error('Error al enviar:', error);
      setMensaje('Error de conexión. Por favor, intenta nuevamente.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="contacto-page">
      <section className="contacto">
        <div className="container">
          <h1>Contactouu</h1>
          <p className="intro">
            Si querés realizar un pedido, consultar disponibilidad o conocer más sobre nuestros productos,
            completá el formulario o comunicate por nuestras redes sociales.
          </p>

          <div className="grid-contacto">
            <form
              className="formulario"
              onSubmit={handleSubmit}
            >
              <label htmlFor="nombre">Nombre:</label>
              <input
                type="text"
                id="nombre"
                name="nombre"
                value={formData.nombre}
                onChange={handleInputChange}
                required
              />

              <label htmlFor="email">Correo electrónico:</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
              />

              <label htmlFor="mensaje">Mensaje:</label>
              <textarea
                id="mensaje"
                name="mensaje"
                rows="5"
                value={formData.mensaje}
                onChange={handleInputChange}
                required
              ></textarea>

              <button type="submit" className="btn" disabled={enviando}>
                {enviando ? 'Enviando...' : 'Enviar mensaje'}
              </button>

              {mensaje && (
                <div className={`mensaje-estado ${mensaje.includes('Error') ? 'error' : 'exito'}`}>
                  {mensaje}
                </div>
              )}
            </form>

            <div className="datos">
              <h3>Datos del emprendimiento</h3>
              <p><strong>Ruta del Higo Catamarca</strong></p>
              <p>📍 El Pantanillo, San Fernando del Valle de Catamarca</p>
              <p>📞 0383 430-6994</p>
              <p>📧 higoscatamarca@gmail.com</p>

              <h3>Seguinos</h3>
              <div className="redes-contacto">
                <a href="#"><img src={facebookIcon} alt="Facebook" /></a>
                <a href="#"><img src={instagramIcon} alt="Instagram" /></a>
                <a href="#"><img src={whatsappIcon} alt="WhatsApp" /></a>
              </div>
            </div>
          </div>

          <div className="mapa">
            <iframe
              src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2670.037536051832!2d-65.80332502568002!3d-28.53130106276765!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x9424297ed9062de9%3A0xab676b250c7a9379!2sParque%20Industrial%20El%20Pantanillo!5e1!3m2!1ses!2sar!4v1761092360858!5m2!1ses!2sar"
              width="100%"
              height="350"
              style={{ border: 0 }}
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