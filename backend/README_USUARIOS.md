# Sistema de Gestión de Usuarios con JWT

## Descripción
Este sistema implementa un CRUD completo de usuarios con autenticación JWT y tres niveles de permisos:

- **ADMIN**: Puede gestionar todos los usuarios y acceder a todos los endpoints
- **MANAGER**: Puede gestionar usuarios compradores y acceder a endpoints específicos
- **COMPRADOR**: Puede registrarse desde afuera y gestionar su propio perfil

## Credenciales por Defecto
- **Usuario**: admin
- **Contraseña**: admin
- **Rol**: ADMIN

## Endpoints Principales

### Autenticación (Públicos)
- `POST /api/auth/registro` - Registro de nuevos usuarios (rol COMPRADOR por defecto)
- `POST /api/auth/login` - Inicio de sesión

### Administración (Solo ADMIN)
- `GET /api/admin/usuarios` - Listar todos los usuarios
- `GET /api/admin/usuarios/{id}` - Obtener usuario por ID
- `POST /api/admin/usuarios` - Crear usuario con rol específico
- `PUT /api/admin/usuarios/{id}` - Actualizar usuario
- `DELETE /api/admin/usuarios/{id}` - Eliminar usuario
- `PATCH /api/admin/usuarios/{id}/estado` - Cambiar estado (activo/inactivo)
- `GET /api/admin/usuarios/role/{role}` - Filtrar usuarios por rol

### Manager (ADMIN y MANAGER)
- `GET /api/manager/usuarios/compradores` - Listar solo usuarios compradores
- `GET /api/manager/usuarios/{id}` - Obtener comprador por ID
- `PATCH /api/manager/usuarios/{id}/estado` - Cambiar estado de compradores

### Usuario (Todos los autenticados)
- `GET /api/user/perfil` - Obtener propio perfil
- `PUT /api/user/perfil` - Actualizar propio perfil

### Productos (Ejemplo de permisos)
- `GET /api/productos` - Listar productos (todos los autenticados)
- `POST /api/productos` - Crear producto (ADMIN y MANAGER)
- `PUT /api/productos/{id}` - Actualizar producto (ADMIN y MANAGER)
- `DELETE /api/productos/{id}` - Eliminar producto (solo ADMIN)

## Flujo de Uso

### 1. Registro de Usuario Comprador
```json
POST /api/auth/registro
{
    "username": "comprador1",
    "email": "comprador1@email.com",
    "password": "123456",
    "nombre": "Juan",
    "apellido": "Pérez",
    "telefono": "123456789",
    "direccion": "Calle 123"
}
```

### 2. Login
```json
POST /api/auth/login
{
    "username": "admin",
    "password": "admin"
}
```
**Respuesta:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "admin",
    "email": "admin@sistema.com",
    "role": "ADMIN"
}
```

### 3. Usar Token en Requests
Agregar el header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Roles y Permisos

### ADMIN
- Gestión completa de usuarios
- Puede crear usuarios con cualquier rol
- Puede eliminar usuarios (excepto otros ADMIN)
- Acceso a todos los endpoints

### MANAGER
- Solo puede gestionar usuarios COMPRADOR
- Puede ver y cambiar estado de compradores
- Acceso a endpoints de productos (crear, actualizar)

### COMPRADOR
- Puede registrarse automáticamente
- Solo puede ver y editar su propio perfil
- Acceso de lectura a productos

## Configuración

### Variables de Entorno
```properties
# JWT
JWT_SECRET=tu_clave_secreta_muy_larga_y_segura_aqui
JWT_EXPIRATION=86400000

# Base de Datos
DB_URL=jdbc:postgresql://localhost:5432/sistema_stock
DB_USS=tu_usuario
DB_PASS=tu_password

# Archivos
FILE_STORAGE_PATH=./files
```

## Swagger/OpenAPI
La documentación interactiva está disponible en:
- http://localhost:8080/swagger-ui.html

## Seguridad
- Las contraseñas se almacenan con hash BCrypt
- Los tokens JWT tienen expiración configurable (24h por defecto)
- Implementa CORS para desarrollo frontend
- Validación de entrada en todos los endpoints

## Estructura del Proyecto
```
src/main/java/com/cisasmendi/sistemastock/
├── config/
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityConfig.java
│   └── ApiDocumentationConfig.java
├── controller/
│   ├── AuthController.java
│   ├── AdminController.java
│   ├── ManagerController.java
│   ├── UserController.java
│   └── ProductoController.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegistroRequest.java
│   │   └── UsuarioUpdateRequest.java
│   └── response/
│       ├── JwtResponse.java
│       └── UsuarioResponse.java
├── model/
│   ├── Role.java
│   └── Usuario.java
├── repository/
│   └── UsuarioRepository.java
└── service/
    ├── AuthService.java
    ├── JwtService.java
    └── UsuarioService.java
```

## Próximos Pasos
1. Integrar con el sistema de productos existente
2. Implementar carrito de compras para usuarios COMPRADOR
3. Agregar sistema de pedidos y órdenes
4. Implementar notificaciones por email
5. Agregar auditoría de acciones de usuarios