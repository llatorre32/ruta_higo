import { getDatabase } from '$lib/database.js';
import { verifySession } from '$lib/auth.js';
import { redirect } from '@sveltejs/kit';

export async function load({ cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session) {
    throw redirect(303, '/');
  }
  
  // Solo administradores pueden acceder a la gestión de usuarios
  if (session.rol !== 'administrador') {
    throw redirect(303, '/dashboard');
  }
  
  return {
    user: {
      id: session.user_id,
      username: session.username,
      nombre: session.nombre,
      rol: session.rol
    }
  };
}