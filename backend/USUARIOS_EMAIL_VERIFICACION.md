# Sistema de Gestión de Usuarios con Verificación por Email

Este sistema implementa un CRUD completo de usuarios con autenticación JWT, verificación por email y recuperación de contraseñas.

## Características Principales

### Roles de Usuario
- **ADMIN**: Control total del sistema, puede gestionar todos los usuarios
- **MANAGER**: Puede gestionar usuarios compradores
- **COMPRADOR**: Usuario final que puede registrarse para realizar compras

### Funcionalidades de Autenticación

#### 1. Registro de Usuario
- **Endpoint**: `POST /api/auth/registro`
- **Descripción**: Permite el registro de nuevos usuarios (por defecto como COMPRADOR)
- **Proceso**:
  1. Usuario se registra con sus datos
  2. Sistema crea cuenta con `emailVerificado = false`
  3. Se envía email con token de verificación
  4. Usuario debe verificar email antes de poder hacer login

#### 2. Verificación de Email
- **Endpoint**: `GET /api/auth/verificar-email?token={token}`
- **Descripción**: Verifica el email del usuario usando el token recibido
- **Token**: Válido por 24 horas

#### 3. Login
- **Endpoint**: `POST /api/auth/login`
- **Descripción**: Autentica usuario y devuelve token JWT
- **Requisitos**: Email debe estar verificado

#### 4. Recuperación de Contraseña
- **Endpoint**: `POST /api/auth/recuperar-password`
- **Descripción**: Solicita recuperación enviando email con token
- **Token**: Válido por 1 hora

#### 5. Restablecer Contraseña
- **Endpoint**: `POST /api/auth/restablecer-password`
- **Descripción**: Restablece contraseña usando token del email

#### 6. Reenviar Verificación
- **Endpoint**: `POST /api/auth/reenviar-verificacion?email={email}`
- **Descripción**: Reenvía email de verificación si no se recibió

## Configuración del Sistema

### Variables de Entorno Requeridas

#### Base de Datos
```
DB_URL=jdbc:postgresql://localhost:5432/sistema_stock
DB_USS=usuario_db
DB_PASS=password_db
```

#### Email (Gmail ejemplo)
```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-password-de-aplicacion
```

#### JWT
```
JWT_SECRET=tu-clave-secreta-muy-larga-y-segura
JWT_EXPIRATION=86400000
```

#### Frontend
```
FRONTEND_URL=http://localhost:3000
```

### Configuración de Gmail

1. Habilitar autenticación de 2 factores en tu cuenta Google
2. Generar una "Contraseña de aplicación" para el correo
3. Usar esa contraseña en `MAIL_PASSWORD`

## Endpoints Disponibles

### Autenticación Pública
- `POST /api/auth/registro` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesión
- `GET /api/auth/verificar-email` - Verificar email
- `POST /api/auth/recuperar-password` - Solicitar recuperación
- `POST /api/auth/restablecer-password` - Restablecer contraseña
- `POST /api/auth/reenviar-verificacion` - Reenviar verificación

### Administración (Solo ADMIN)
- `GET /api/admin/usuarios` - Listar todos los usuarios
- `GET /api/admin/usuarios/{id}` - Obtener usuario por ID
- `POST /api/admin/usuarios` - Crear usuario con rol específico
- `PUT /api/admin/usuarios/{id}` - Actualizar usuario
- `DELETE /api/admin/usuarios/{id}` - Eliminar usuario
- `PATCH /api/admin/usuarios/{id}/estado` - Cambiar estado usuario
- `GET /api/admin/usuarios/role/{role}` - Usuarios por rol

### Manager (ADMIN y MANAGER)
- `GET /api/manager/usuarios/compradores` - Listar compradores
- `GET /api/manager/usuarios/{id}` - Ver comprador específico
- `PATCH /api/manager/usuarios/{id}/estado` - Cambiar estado comprador

## Usuario Administrador por Defecto

El sistema crea automáticamente un usuario administrador:
- **Username**: `admin`
- **Password**: `admin`
- **Email**: `admin@sistema.com`
- **Rol**: `ADMIN`
- **Email Verificado**: `true` (no requiere verificación)

## Flujo de Registro y Verificación

1. **Usuario se registra**:
   ```json
   POST /api/auth/registro
   {
     "username": "juan123",
     "email": "juan@example.com",
     "password": "password123",
     "nombre": "Juan",
     "apellido": "Pérez",
     "telefono": "555-1234",
     "direccion": "Calle 123"
   }
   ```

2. **Sistema responde** con datos del usuario (sin password)

3. **Usuario recibe email** con enlace:
   ```
   http://localhost:3000/verificar-email?token=abc123def456...
   ```

4. **Frontend llama endpoint de verificación**:
   ```
   GET /api/auth/verificar-email?token=abc123def456...
   ```

5. **Usuario puede hacer login**:
   ```json
   POST /api/auth/login
   {
     "username": "juan123",
     "password": "password123"
   }
   ```

## Flujo de Recuperación de Contraseña

1. **Usuario solicita recuperación**:
   ```json
   POST /api/auth/recuperar-password
   {
     "email": "juan@example.com"
   }
   ```

2. **Usuario recibe email** con enlace:
   ```
   http://localhost:3000/restablecer-password?token=xyz789abc123...
   ```

3. **Frontend permite cambiar contraseña**:
   ```json
   POST /api/auth/restablecer-password
   {
     "token": "xyz789abc123...",
     "nuevaPassword": "nueva_password123"
   }
   ```

## Seguridad

- Las contraseñas se encriptan con BCrypt
- Los tokens JWT incluyen información del rol del usuario
- Los endpoints están protegidos por roles usando Spring Security
- Los tokens de verificación expiran automáticamente
- Sistema de limpieza automática de tokens expirados (cada hora)

## Documentación API

La documentación completa está disponible en:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/api/v3/api-docs`

## Errores Comunes

1. **"La cuenta no está verificada"**: El usuario debe verificar su email primero
2. **"Token inválido o ya utilizado"**: El token ya fue usado o expiró
3. **"Error enviando email"**: Verificar configuración de SMTP
4. **"El email ya está registrado"**: Ya existe un usuario con ese email

## Desarrollo

Para desarrollo local, puedes usar configuraciones por defecto en `application.properties` o crear un archivo `.env` en la raíz del proyecto.