import { json } from '@sveltejs/kit';
import { verifySession, changePassword } from '$lib/auth.js';

// POST - Cambiar contraseña
export async function POST({ request, cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }
  
  try {
    const { currentPassword, newPassword, confirmPassword } = await request.json();
    
    // Validaciones
    if (!currentPassword || !newPassword || !confirmPassword) {
      return json({ error: 'Todos los campos son requeridos' }, { status: 400 });
    }
    
    if (newPassword !== confirmPassword) {
      return json({ error: 'Las contraseñas no coinciden' }, { status: 400 });
    }
    
    if (newPassword.length < 6) {
      return json({ error: 'La nueva contraseña debe tener al menos 6 caracteres' }, { status: 400 });
    }
    
    if (currentPassword === newPassword) {
      return json({ error: 'La nueva contraseña debe ser diferente a la actual' }, { status: 400 });
    }
    
    // Cambiar contraseña
    const success = changePassword(session.user_id, currentPassword, newPassword);
    
    if (success) {
      return json({ success: true, message: 'Contraseña cambiada correctamente' });
    } else {
      return json({ error: 'Contraseña actual incorrecta' }, { status: 400 });
    }
  } catch (error) {
    console.error('Error al cambiar contraseña:', error);
    return json({ error: 'Error interno del servidor' }, { status: 500 });
  }
}