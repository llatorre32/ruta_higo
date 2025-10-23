import { Link, useLocation } from 'react-router-dom';
import logo from '../assets/img/logos/Logo_ruta_higo.png';
import './Header.css';

const Header = () => {
  const location = useLocation();

  return (
    <header>
      <div className="container header-content">
        <img src={logo} alt="Ruta del Higo Catamarca" className="logo" />
        <nav>
          <ul className="menu">
            <li>
              <Link 
                to="/" 
                className={location.pathname === '/' ? 'active' : ''}
              >
                Inicio
              </Link>
            </li>
            <li>
              <Link 
                to="/productos" 
                className={location.pathname === '/productos' ? 'active' : ''}
              >
                Productos
              </Link>
            </li>
            <li>
              <Link 
                to="/nosotros" 
                className={location.pathname === '/nosotros' ? 'active' : ''}
              >
                Nosotros
              </Link>
            </li>
            <li>
              <Link 
                to="/contacto" 
                className={location.pathname === '/contacto' ? 'active' : ''}
              >
                Contacto
              </Link>
            </li>
          </ul>
        </nav>
      </div>
    </header>
  );
};

export default Header;