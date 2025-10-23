import facebookIcon from '../assets/img/facebook.png';
import instagramIcon from '../assets/img/instagram.png';
import whatsappIcon from '../assets/img/whatsapp.png';
import './Footer.css';

const Footer = () => {
  return (
    <footer>
      <div className="container">
        <p>&copy; 2025 Ruta del Higo Catamarca. Todos los derechos reservados.</p>
        <div className="redes">
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
    </footer>
  );
};

export default Footer;