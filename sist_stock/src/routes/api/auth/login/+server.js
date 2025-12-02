import { authenticateUser, createSession } from '$lib/auth.js';
import { json } from '@sveltejs/kit';

export async function POST({ request, cookies }) {
  try {
    const { username, password } = await request.json();
    
    if (!username || !password) {
      return json({ error: 'Usuario y contraseña son requeridos' }, { status: 400 });
    }
    
    const user = authenticateUser(username, password);
    
    if (!user) {
      return json({ error: 'Credenciales inválidas' }, { status: 401 });
    }
    
    const session = createSession(user.id);
    
    // Establecer cookie de sesión
    cookies.set('session_token', session.token, {
      path: '/',
      httpOnly: true,
      secure: false, // Cambiar a true en producción con HTTPS
      sameSite: 'strict',
      expires: session.expiresAt
    });
    
    return json({ 
      success: true, 
      user: {
        id: user.id,
        username: user.username,
        nombre: user.nombre,
        email: user.email,
        rol: user.rol
      }
    });
    
  } catch (error) {
    console.error('Error en login:', error);
    return json({ error: 'Error interno del servidor' }, { status: 500 });
  }
}