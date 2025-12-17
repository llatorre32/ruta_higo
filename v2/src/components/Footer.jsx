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
          <a href="https://www.facebook.com/profile.php?id=100086834407348" target="_blank" rel="noopener noreferrer" aria-label="Facebook">
            <img src={facebookIcon} alt="Facebook" />
          </a>
          <a href="https://www.instagram.com/rutadelhigo.catamarca/" target="_blank" rel="noopener noreferrer" aria-label="Instagram">
            <img src={instagramIcon} alt="Instagram" />
          </a>
          <a href="https://wa.me/5493834306994" target="_blank" rel="noopener noreferrer" aria-label="WhatsApp">
            <img src={whatsappIcon} alt="WhatsApp" />
          </a>
        
        </div>
      </div>
    </footer>
  );
};

export default Footer;