import { verifySession } from '$lib/auth.js';
import { redirect } from '@sveltejs/kit';

export async function load({ cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session) {
    throw redirect(303, '/');
  }
  
  return {
    user: {
      id: session.user_id,
      username: session.username,
      nombre: session.nombre,
      email: session.email,
      rol: session.rol
    }
  };
}