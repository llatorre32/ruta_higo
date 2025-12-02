import { verifySession } from '$lib/auth.js';
import { initDatabase } from '$lib/database.js';

// Inicializar la base de datos al arrancar el servidor
initDatabase();

export async function handle({ event, resolve }) {
  // Obtener el token de las cookies
  const token = event.cookies.get('session_token');
  
  if (token) {
    const session = verifySession(token);
    if (session) {
      event.locals.user = {
        id: session.user_id,
        username: session.username,
        nombre: session.nombre,
        email: session.email,
        rol: session.rol
      };
    } else {
      // Token inválido o expirado, eliminar cookie
      event.cookies.delete('session_token', { path: '/' });
    }
  }
  
  return resolve(event);
}