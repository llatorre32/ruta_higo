import bcrypt from 'bcryptjs';
import { v4 as uuidv4 } from 'uuid';
import { getDatabase } from './database.js';

// Verificar credenciales de usuario
export function authenticateUser(username, password) {
  const db = getDatabase();
  const user = db.prepare('SELECT * FROM usuarios WHERE username = ? AND activo = 1').get(username);
  
  if (!user) {
    return null;
  }
  
  const isValidPassword = bcrypt.compareSync(password, user.password);
  if (!isValidPassword) {
    return null;
  }
  
  // Eliminar la contraseña del objeto usuario
  const { password: _, ...userWithoutPassword } = user;
  return userWithoutPassword;
}

// Crear sesión de usuario
export function createSession(userId) {
  const db = getDatabase();
  const sessionId = uuidv4();
  const token = uuidv4();
  const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 horas
  
  // Eliminar sesiones expiradas del usuario
  db.prepare('DELETE FROM sesiones WHERE usuario_id = ? OR expires_at < ?')
    .run(userId, new Date().toISOString());
  
  // Crear nueva sesión
  db.prepare(`
    INSERT INTO sesiones (id, usuario_id, token, expires_at)
    VALUES (?, ?, ?, ?)
  `).run(sessionId, userId, token, expiresAt.toISOString());
  
  return { sessionId, token, expiresAt };
}

// Verificar sesión
export function verifySession(token) {
  const db = getDatabase();
  const session = db.prepare(`
    SELECT s.*, u.id as user_id, u.username, u.nombre, u.email, u.rol
    FROM sesiones s
    JOIN usuarios u ON s.usuario_id = u.id
    WHERE s.token = ? AND s.expires_at > ? AND u.activo = 1
  `).get(token, new Date().toISOString());
  
  return session || null;
}

// Cerrar sesión
export function logout(token) {
  const db = getDatabase();
  return db.prepare('DELETE FROM sesiones WHERE token = ?').run(token).changes > 0;
}

// Obtener usuario por ID
export function getUserById(userId) {
  const db = getDatabase();
  const user = db.prepare('SELECT id, username, nombre, email, rol, activo FROM usuarios WHERE id = ?').get(userId);
  return user || null;
}

// Cambiar contraseña
export function changePassword(userId, currentPassword, newPassword) {
  const db = getDatabase();
  const user = db.prepare('SELECT password FROM usuarios WHERE id = ?').get(userId);
  
  if (!user || !bcrypt.compareSync(currentPassword, user.password)) {
    return false;
  }
  
  const hashedNewPassword = bcrypt.hashSync(newPassword, 10);
  const result = db.prepare('UPDATE usuarios SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?')
    .run(hashedNewPassword, userId);
  
  return result.changes > 0;
}

// Obtener todos los usuarios (solo administradores)
export function getAllUsers(page = 1, limit = 10, search = '') {
  const db = getDatabase();
  const offset = (page - 1) * limit;
  
  let query = `
    SELECT id, username, nombre, email, rol, activo, created_at, updated_at
    FROM usuarios
  `;
  let countQuery = 'SELECT COUNT(*) as total FROM usuarios';
  let params = [];
  
  if (search) {
    const searchCondition = ' WHERE (username LIKE ? OR nombre LIKE ? OR email LIKE ?)';
    query += searchCondition;
    countQuery += searchCondition;
    const searchParam = `%${search}%`;
    params = [searchParam, searchParam, searchParam];
  }
  
  query += ' ORDER BY created_at DESC LIMIT ? OFFSET ?';
  params.push(limit, offset);
  
  const usuarios = db.prepare(query).all(...params);
  const totalResult = db.prepare(countQuery).get(...(search ? [search, search, search] : []));
  
  return {
    usuarios,
    total: totalResult.total,
    pages: Math.ceil(totalResult.total / limit)
  };
}

// Crear usuario
export function createUser(username, password, nombre, email, rol) {
  const db = getDatabase();
  
  // Verificar si ya existe el username o email
  const existingUser = db.prepare('SELECT id FROM usuarios WHERE username = ? OR email = ?').get(username, email);
  if (existingUser) {
    throw new Error('El usuario o email ya existe');
  }
  
  const id = uuidv4();
  const hashedPassword = bcrypt.hashSync(password, 10);
  
  const result = db.prepare(`
    INSERT INTO usuarios (id, username, password, nombre, email, rol)
    VALUES (?, ?, ?, ?, ?, ?)
  `).run(id, username, hashedPassword, nombre, email, rol);
  
  if (result.changes > 0) {
    return db.prepare(`
      SELECT id, username, nombre, email, rol, activo, created_at
      FROM usuarios WHERE id = ?
    `).get(id);
  }
  
  throw new Error('Error al crear usuario');
}

// Actualizar usuario
export function updateUser(userId, data) {
  const db = getDatabase();
  
  const updateFields = [];
  const updateValues = [];
  
  if (data.username !== undefined) {
    // Verificar que el username no exista en otro usuario
    const usernameExists = db.prepare('SELECT id FROM usuarios WHERE username = ? AND id != ?').get(data.username, userId);
    if (usernameExists) {
      throw new Error('El nombre de usuario ya existe');
    }
    updateFields.push('username = ?');
    updateValues.push(data.username);
  }
  
  if (data.nombre !== undefined) {
    updateFields.push('nombre = ?');
    updateValues.push(data.nombre);
  }
  
  if (data.email !== undefined) {
    // Verificar que el email no exista en otro usuario
    const emailExists = db.prepare('SELECT id FROM usuarios WHERE email = ? AND id != ?').get(data.email, userId);
    if (emailExists) {
      throw new Error('El email ya existe');
    }
    updateFields.push('email = ?');
    updateValues.push(data.email);
  }
  
  if (data.rol !== undefined) {
    updateFields.push('rol = ?');
    updateValues.push(data.rol);
  }
  
  if (data.activo !== undefined) {
    updateFields.push('activo = ?');
    updateValues.push(data.activo ? 1 : 0);
  }
  
  if (updateFields.length === 0) {
    throw new Error('No hay campos para actualizar');
  }
  
  updateFields.push('updated_at = CURRENT_TIMESTAMP');
  updateValues.push(userId);
  
  const query = `UPDATE usuarios SET ${updateFields.join(', ')} WHERE id = ?`;
  const result = db.prepare(query).run(...updateValues);
  
  if (result.changes > 0) {
    return db.prepare(`
      SELECT id, username, nombre, email, rol, activo, created_at, updated_at
      FROM usuarios WHERE id = ?
    `).get(userId);
  }
  
  throw new Error('Error al actualizar usuario');
}

// Desactivar usuario
export function deactivateUser(userId) {
  const db = getDatabase();
  const result = db.prepare('UPDATE usuarios SET activo = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?').run(userId);
  return result.changes > 0;
}