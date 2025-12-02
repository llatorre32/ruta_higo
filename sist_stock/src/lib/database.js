import Database from 'better-sqlite3';
import bcrypt from 'bcryptjs';
import { v4 as uuidv4 } from 'uuid';

let db = null;

// Inicializar la base de datos
export function initDatabase() {
  if (db) return db;
  
  db = new Database('sist_stock.db');
  
  // Crear tablas
  createTables();
  
  // Insertar usuarios por defecto si no existen
  insertDefaultUsers();
  
  return db;
}

function createTables() {
  // Tabla de usuarios
  db.exec(`
    CREATE TABLE IF NOT EXISTS usuarios (
      id TEXT PRIMARY KEY,
      username TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      nombre TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      rol TEXT NOT NULL CHECK (rol IN ('administrador', 'manejador')),
      activo INTEGER DEFAULT 1,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Tabla de categorías
  db.exec(`
    CREATE TABLE IF NOT EXISTS categorias (
      id TEXT PRIMARY KEY,
      nombre TEXT UNIQUE NOT NULL,
      descripcion TEXT,
      activo INTEGER DEFAULT 1,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Tabla de productos
  db.exec(`
    CREATE TABLE IF NOT EXISTS productos (
      id TEXT PRIMARY KEY,
      codigo TEXT UNIQUE NOT NULL,
      nombre TEXT NOT NULL,
      descripcion TEXT,
      categoria_id TEXT,
      precio_compra REAL DEFAULT 0,
      precio_venta REAL DEFAULT 0,
      stock_minimo INTEGER DEFAULT 0,
      stock_maximo INTEGER DEFAULT 0,
      stock_actual INTEGER DEFAULT 0,
      imagen TEXT,
      activo INTEGER DEFAULT 1,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (categoria_id) REFERENCES categorias(id)
    )
  `);

  // Agregar columna imagen si no existe (para bases de datos existentes)
  try {
    db.exec(`ALTER TABLE productos ADD COLUMN imagen TEXT`);
  } catch (error) {
    // La columna ya existe, ignorar error
  }

  // Tabla de movimientos de stock
  db.exec(`
    CREATE TABLE IF NOT EXISTS movimientos_stock (
      id TEXT PRIMARY KEY,
      producto_id TEXT NOT NULL,
      tipo TEXT NOT NULL CHECK (tipo IN ('entrada', 'salida', 'ajuste')),
      cantidad INTEGER NOT NULL,
      motivo TEXT,
      usuario_id TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (producto_id) REFERENCES productos(id),
      FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    )
  `);

  // Tabla de sesiones
  db.exec(`
    CREATE TABLE IF NOT EXISTS sesiones (
      id TEXT PRIMARY KEY,
      usuario_id TEXT NOT NULL,
      token TEXT UNIQUE NOT NULL,
      expires_at DATETIME NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    )
  `);

  // Tabla de ventas
  db.exec(`
    CREATE TABLE IF NOT EXISTS ventas (
      id TEXT PRIMARY KEY,
      numero_venta TEXT UNIQUE NOT NULL,
      cliente_nombre TEXT,
      cliente_documento TEXT,
      cliente_telefono TEXT,
      cliente_email TEXT,
      subtotal REAL NOT NULL DEFAULT 0,
      descuento REAL DEFAULT 0,
      impuestos REAL DEFAULT 0,
      total REAL NOT NULL DEFAULT 0,
      metodo_pago TEXT DEFAULT 'efectivo',
      estado TEXT DEFAULT 'completada' CHECK (estado IN ('completada', 'cancelada', 'pendiente')),
      usuario_id TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    )
  `);

  // Tabla de detalles de ventas
  db.exec(`
    CREATE TABLE IF NOT EXISTS ventas_detalle (
      id TEXT PRIMARY KEY,
      venta_id TEXT NOT NULL,
      producto_id TEXT NOT NULL,
      cantidad INTEGER NOT NULL,
      precio_unitario REAL NOT NULL,
      subtotal REAL NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (venta_id) REFERENCES ventas(id),
      FOREIGN KEY (producto_id) REFERENCES productos(id)
    )
  `);

  // Tabla de clientes
  db.exec(`
    CREATE TABLE IF NOT EXISTS clientes (
      id TEXT PRIMARY KEY,
      nombre TEXT NOT NULL,
      documento TEXT,
      telefono TEXT,
      email TEXT,
      direccion TEXT,
      activo INTEGER DEFAULT 1,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Tabla de reportes predefinidos
  db.exec(`
    CREATE TABLE IF NOT EXISTS reportes (
      id TEXT PRIMARY KEY,
      nombre TEXT NOT NULL,
      descripcion TEXT,
      tipo TEXT NOT NULL CHECK (tipo IN ('ventas', 'stock', 'productos', 'movimientos')),
      query TEXT NOT NULL,
      parametros TEXT, -- JSON con parámetros del reporte
      activo INTEGER DEFAULT 1,
      created_by TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (created_by) REFERENCES usuarios(id)
    )
  `);

  // Crear índices
  db.exec(`
    CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos(codigo);
    CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria_id);
    CREATE INDEX IF NOT EXISTS idx_movimientos_producto ON movimientos_stock(producto_id);
    CREATE INDEX IF NOT EXISTS idx_movimientos_usuario ON movimientos_stock(usuario_id);
    CREATE INDEX IF NOT EXISTS idx_sesiones_token ON sesiones(token);
    CREATE INDEX IF NOT EXISTS idx_sesiones_usuario ON sesiones(usuario_id);
    CREATE INDEX IF NOT EXISTS idx_ventas_usuario ON ventas(usuario_id);
    CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(created_at);
    CREATE INDEX IF NOT EXISTS idx_ventas_detalle_venta ON ventas_detalle(venta_id);
    CREATE INDEX IF NOT EXISTS idx_ventas_detalle_producto ON ventas_detalle(producto_id);
    CREATE INDEX IF NOT EXISTS idx_clientes_documento ON clientes(documento);
    CREATE INDEX IF NOT EXISTS idx_reportes_tipo ON reportes(tipo);
  `);
}

function insertDefaultUsers() {
  const checkUser = db.prepare('SELECT COUNT(*) as count FROM usuarios');
  const userCount = checkUser.get().count;
  
  if (userCount === 0) {
    const adminPassword = bcrypt.hashSync('admin123', 10);
    const manejadorPassword = bcrypt.hashSync('manager123', 10);
    
    const insertUser = db.prepare(`
      INSERT INTO usuarios (id, username, password, nombre, email, rol)
      VALUES (?, ?, ?, ?, ?, ?)
    `);
    
    // Usuario administrador
    insertUser.run(
      uuidv4(),
      'admin',
      adminPassword,
      'Administrador del Sistema',
      'admin@sistema.com',
      'administrador'
    );
    
    // Usuario manejador
    insertUser.run(
      uuidv4(),
      'manager',
      manejadorPassword,
      'Manejador de Stock',
      'manager@sistema.com',
      'manejador'
    );
    
    console.log('Usuarios por defecto creados:');
    console.log('Administrador - Usuario: admin, Contraseña: admin123');
    console.log('Manejador - Usuario: manager, Contraseña: manager123');
  }
}

// Función para obtener la instancia de la base de datos
export function getDatabase() {
  if (!db) {
    initDatabase();
  }
  return db;
}

// Cerrar la base de datos
export function closeDatabase() {
  if (db) {
    db.close();
    db = null;
  }
}