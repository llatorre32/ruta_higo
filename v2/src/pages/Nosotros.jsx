import equipoImg from '../assets/img/nosotros/equipo.jpg';
import recoleccionImg from '../assets/img/nosotros/recoleccion.jpg';
import elaboracionImg from '../assets/img/nosotros/elaboracion.jpg';
import envasadoImg from '../assets/img/nosotros/envasado.jpg';
import misionImg from '../assets/img/nosotros/mision.jpg';
import './Nosotros.css';

const Nosotros = () => {
  return (
    <div className="nosotros-page">
      {/* Sección presentación */}
      <section className="presentacion">
        <div className="container">
          <div className="texto">
            <h1>Sobre Nosotros</h1>
            <p>
              Somos un emprendimiento catamarqueño apasionado por la producción artesanal de <strong>dulces, higos en almíbar y plantines de higuera</strong>.  
              En <strong>Ruta del Higo</strong> fusionamos la tradición familiar con técnicas naturales y procesos cuidadosos para ofrecer productos de calidad, auténticos y sustentables.  
              Cada frasco que elaboramos refleja nuestra dedicación, el respeto por la naturaleza y el amor por la gastronomía local.
            </p>
            <p>
              Nos enorgullece trabajar con pequeños productores locales, fortaleciendo la economía regional y preservando la identidad de Catamarca.  
              Nuestro compromiso va más allá del sabor: buscamos transmitir historias y tradiciones en cada higo que llega a tu mesa.
            </p>
          </div>
          <img src={equipoImg} alt="Equipo de trabajo artesanal" className="imagen" />
        </div>
      </section>

      {/* Sección del proceso */}
      <section className="proceso">
        <div className="container">
          <h2>Nuestro Proceso Artesanal</h2>
          <div className="grid-proceso">
            <div className="paso">
              <img src={recoleccionImg} alt="Recolección de higos" />
              <h3>1. Recolección</h3>
              <p>
                Seleccionamos cuidadosamente los higos más frescos y maduros de productores locales.  
                Cada fruto es inspeccionado para garantizar su calidad y preservar el sabor auténtico de la fruta.
              </p>
            </div>
            <div className="paso">
              <img src={elaboracionImg} alt="Elaboración artesanal" />
              <h3>2. Elaboración</h3>
              <p>
                Elaboramos nuestros productos en pequeñas tandas, siguiendo recetas tradicionales transmitidas de generación en generación.  
                El proceso artesanal nos permite controlar cada detalle y mantener la autenticidad y textura de nuestros dulces.
              </p>
            </div>
            <div className="paso">
              <img src={envasadoImg} alt="Envasado artesanal" />
              <h3>3. Envasado</h3>
              <p>
                Nuestros productos se envasan en frascos esterilizados y empaques reciclables, asegurando frescura y sostenibilidad.  
                Cada frasco es un reflejo de nuestro cuidado por la calidad y el medio ambiente.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Sección de misión */}
      <section className="mision">
        <div className="container">
          <h2>Nuestra Misión</h2>
          <p>
            Nuestra misión es promover el desarrollo local mediante productos sustentables que reflejen la esencia de Catamarca.  
            Buscamos fortalecer la identidad regional y ofrecer a nuestros clientes experiencias únicas a través de sabores genuinos y naturales.
          </p>
          <p>
            Creemos en un modelo de trabajo que respeta la naturaleza, apoya a los pequeños productores y mantiene viva la tradición artesanal.  
            Cada dulce, higo en almíbar o plantín que entregamos está impregnado de pasión, cuidado y dedicación.
          </p>
          <img src={misionImg} alt="Producción artesanal de higos" className="imagen-mision" />
        </div>
      </section>
    </div>
  );
};

export default Nosotros;