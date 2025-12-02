import { json } from '@sveltejs/kit';
import { getDatabase } from '$lib/database.js';
import { verifySession } from '$lib/auth.js';
import bcrypt from 'bcryptjs';
import { v4 as uuidv4 } from 'uuid';

// GET - Obtener lista de usuarios (solo administradores)
export async function GET({ url, cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session || session.rol !== 'administrador') {
    return json({ error: 'No autorizado' }, { status: 403 });
  }
  
  const db = getDatabase();
  const page = parseInt(url.searchParams.get('page') || '1');
  const limit = parseInt(url.searchParams.get('limit') || '10');
  const search = url.searchParams.get('search') || '';
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
  
  try {
    const usuarios = db.prepare(query).all(...params);
    const totalResult = db.prepare(countQuery).get(...(search ? [search, search, search] : []));
    const total = totalResult.total;
    
    return json({
      usuarios,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Error al obtener usuarios:', error);
    return json({ error: 'Error interno del servidor' }, { status: 500 });
  }
}

// POST - Crear nuevo usuario (solo administradores)
export async function POST({ request, cookies }) {
  const token = cookies.get('session_token');
  const session = verifySession(token);
  
  if (!session || session.rol !== 'administrador') {
    return json({ error: 'No autorizado' }, { status: 403 });
  }
  
  try {
    const { username, password, nombre, email, rol } = await request.json();
    
    // Validaciones
    if (!username || !password || !nombre || !email || !rol) {
      return json({ error: 'Todos los campos son requeridos' }, { status: 400 });
    }
    
    if (password.length < 6) {
      return json({ error: 'La contraseña debe tener al menos 6 caracteres' }, { status: 400 });
    }
    
    if (!['administrador', 'manejador'].includes(rol)) {
      return json({ error: 'Rol no válido' }, { status: 400 });
    }
    
    // Validar email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return json({ error: 'Email no válido' }, { status: 400 });
    }
    
    const db = getDatabase();
    
    // Verificar si ya existe el username o email
    const existingUser = db.prepare('SELECT id FROM usuarios WHERE username = ? OR email = ?').get(username, email);
    if (existingUser) {
      return json({ error: 'El usuario o email ya existe' }, { status: 400 });
    }
    
    // Crear usuario
    const id = uuidv4();
    const hashedPassword = bcrypt.hashSync(password, 10);
    
    const result = db.prepare(`
      INSERT INTO usuarios (id, username, password, nombre, email, rol)
      VALUES (?, ?, ?, ?, ?, ?)
    `).run(id, username, hashedPassword, nombre, email, rol);
    
    if (result.changes > 0) {
      const newUser = db.prepare(`
        SELECT id, username, nombre, email, rol, activo, created_at
        FROM usuarios WHERE id = ?
      `).get(id);
      
      return json({ success: true, usuario: newUser }, { status: 201 });
    } else {
      return json({ error: 'Error al crear usuario' }, { status: 500 });
    }
  } catch (error) {
    console.error('Error al crear usuario:', error);
    return json({ error: 'Error interno del servidor' }, { status: 500 });
  }
}