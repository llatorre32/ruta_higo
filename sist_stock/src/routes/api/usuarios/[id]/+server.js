import { json } from '@sveltejs/kit';
import { getDatabase } from '$lib/database.js';
import { verifySession } from '$lib/auth.js';
import bcrypt from 'bcryptjs';

// GET - Obtener usuario específico
export async function GET({ params, cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }
  
  const { id } = params;
  
  // Solo administradores pueden ver cualquier usuario, otros usuarios solo pueden ver su propio perfil
  if (session.rol !== 'administrador' && session.user_id !== id) {
    return json({ error: 'No autorizado' }, { status: 403 });
  }
  
  const db = getDatabase();
  
  try {
    const usuario = db.prepare(`
      SELECT id, username, nombre, email, rol, activo, created_at, updated_at
      FROM usuarios WHERE id = ?
    `).get(id);
    
    if (!usuario) {
      return json({ error: 'Usuario no encontrado' }, { status: 404 });
    }
    
    return json({ usuario });
  } catch (error) {
    console.error('Error al obtener usuario:', error);
    return json({ error: 'Error interno del servidor' }, { status: 500 });
  }
}

// PUT - Actualizar usuario
export async function PUT({ params, request, cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session) {
    return json({ error: 'No autorizado' }, { status: 401 });
  }
  
  const { id } = params;
  
  // Solo administradores pueden editar cualquier usuario, otros usuarios solo pueden editar su propio perfil
  if (session.rol !== 'administrador' && session.user_id !== id) {
    return json({ error: 'No autorizado' }, { status: 403 });
  }
  
  try {
    const data = await request.json();
    const db = getDatabase();
    
    // Verificar que el usuario existe
    const existingUser = db.prepare('SELECT * FROM usuarios WHERE id = ?').get(id);
    if (!existingUser) {
      return json({ error: 'Usuario no encontrado' }, { status: 404 });
    }
    
    let updateFields = [];
    let updateValues = [];
    
    // Campos que puede editar un administrador
    if (session.rol === 'administrador') {
      if (data.username !== undefined) {
        // Verificar que el username no exista en otro usuario
        const usernameExists = db.prepare('SELECT id FROM usuarios WHERE username = ? AND id != ?').get(data.username, id);
        if (usernameExists) {
          return json({ error: 'El nombre de usuario ya existe' }, { status: 400 });
        }
        updateFields.push('username = ?');
        updateValues.push(data.username);
      }
      
      if (data.rol !== undefined && ['administrador', 'manejador'].includes(data.rol)) {
        updateFields.push('rol = ?');
        updateValues.push(data.rol);
      }
      
      if (data.activo !== undefined) {
        updateFields.push('activo = ?');
        updateValues.push(data.activo ? 1 : 0);
      }
    }
    
    // Campos que puede editar cualquier usuario en su propio perfil
    if (data.nombre !== undefined) {
      updateFields.push('nombre = ?');
      updateValues.push(data.nombre);
    }
    
    if (data.email !== undefined) {
      // Validar email
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(data.email)) {
        return json({ error: 'Email no válido' }, { status: 400 });
      }
      
      // Verificar que el email no exista en otro usuario
      const emailExists = db.prepare('SELECT id FROM usuarios WHERE email = ? AND id != ?').get(data.email, id);
      if (emailExists) {
        return json({ error: 'El email ya existe' }, { status: 400 });
      }
      
      updateFields.push('email = ?');
      updateValues.push(data.email);
    }
    
    if (updateFields.length === 0) {
      return json({ error: 'No hay campos para actualizar' }, { status: 400 });
    }
    
    updateFields.push('updated_at = CURRENT_TIMESTAMP');
    updateValues.push(id);
    
    const query = `UPDATE usuarios SET ${updateFields.join(', ')} WHERE id = ?`;
    const result = db.prepare(query).run(...updateValues);
    
    if (result.changes > 0) {
      const updatedUser = db.prepare(`
        SELECT id, username, nombre, email, rol, activo, created_at, updated_at
        FROM usuarios WHERE id = ?
      `).get(id);
      
      return json({ success: true, usuario: updatedUser });
    } else {
      return json({ error: 'Error al actualizar usuario' }, { status: 500 });
    }
  } catch (error) {
    console.error('Error al actualizar usuario:', error);
    return json({ error: 'Error interno del servidor' }, { status: 500 });
  }
}

// DELETE - Eliminar usuario (solo administradores)
export async function DELETE({ params, cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session || session.rol !== 'administrador') {
    return json({ error: 'No autorizado' }, { status: 403 });
  }
  
  const { id } = params;
  
  // No permitir que el administrador se elimine a sí mismo
  if (session.user_id === id) {
    return json({ error: 'No puedes eliminar tu propia cuenta' }, { status: 400 });
  }
  
  const db = getDatabase();
  
  try {
    // Verificar que el usuario existe
    const existingUser = db.prepare('SELECT * FROM usuarios WHERE id = ?').get(id);
    if (!existingUser) {
      return json({ error: 'Usuario no encontrado' }, { status: 404 });
    }
    
    // En lugar de eliminar, desactivar el usuario para mantener integridad referencial
    const result = db.prepare('UPDATE usuarios SET activo = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?').run(id);
    
    if (result.changes > 0) {
      return json({ success: true, message: 'Usuario desactivado correctamente' });
    } else {
      return json({ error: 'Error al eliminar usuario' }, { status: 500 });
    }
  } catch (error) {
    console.error('Error al eliminar usuario:', error);
    return json({ error: 'Error interno del servidor' }, { status: 500 });
  }
}