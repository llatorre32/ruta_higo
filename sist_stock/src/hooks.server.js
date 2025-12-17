import { verifySession } from '$lib/auth.js';
import { initDatabase } from '$lib/database.js';

// Inicializar la base de datos al arrancar el servidor
initDatabase();

export async function handle({ event, resolve }) {

  // 👉 CORS: responder preflight
  if (event.request.method === 'OPTIONS') {
    return new Response(null, {
      status: 204,
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:5174',
        'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
        'Access-Control-Allow-Credentials': 'true'
      }
    });
  }

  // 👉 Sesión
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
      event.cookies.delete('session_token', { path: '/' });
    }
  }

  const response = await resolve(event);

  // 👉 CORS: headers en respuesta normal
  response.headers.set('Access-Control-Allow-Origin', '*');
  response.headers.set('Access-Control-Allow-Credentials', 'true');

  return response;
}
