# Sistema de Stock y Ventas - Backend API

API REST para gestión de stock, ventas y usuarios basada en las historias de usuario del proyecto.

## 🚀 Características Implementadas

### 👤 Gestión de Usuarios
- Registro y autenticación con JWT
- Verificación de email
- Recuperación de contraseña
- Roles: ADMIN, MANAGER, COMPRADOR

### 📦 Gestión de Productos
- CRUD completo de productos
- Búsqueda y filtros
- Control de stock con alertas
- **Upload de imágenes (múltiples por producto)**
- Validación de formatos y tamaños de imagen
- Almacenamiento organizado por producto

### 🛒 Carrito de Compras
- Agregar productos al carrito
- Actualizar cantidades
- Eliminar productos
- Reserva de stock por 3 días
- Limpieza automática de reservas expiradas

### 📦 Pedidos
- Crear pedidos desde el carrito
- Estados: PENDIENTE_PAGO, PAGADO, EN_DESPACHO, ENTREGADO, CANCELADO
- Confirmación de pago por managers
- Asignación de código de envío
- Ventas presenciales
- Notificaciones por email

### 📊 Productos
- CRUD de productos
- Búsqueda y filtros
- Control de stock
- Alertas de stock bajo
- Precio y disponibilidad

### 📈 Reportes y Auditoría
- Reportes de ventas por rango de fechas
- Productos más vendidos
- Ventas por día
- Auditoría de operaciones
- Seguimiento de usuarios

## 🏗️ Arquitectura

```
backend/
├── src/main/java/com/cisasmendi/sistemastock/
│   ├── config/           # Configuración (Security, CORS, Tasks)
│   ├── controller/       # Endpoints REST
│   ├── dto/             # Data Transfer Objects
│   │   ├── request/     # DTOs de entrada
│   │   └── response/    # DTOs de salida
│   ├── model/           # Entidades JPA
│   ├── repository/      # Repositorios de datos
│   ├── service/         # Lógica de negocio
│   └── exception/       # Excepciones personalizadas
└── src/main/resources/
    └── application.properties
```

## 📋 Entidades Principales

### Usuario
- Campos: username, email, password, nombre, apellido, role, activo, emailVerificado
- Roles: ADMIN, MANAGER, COMPRADOR

### Producto
- Campos: nombre, descripción, precio, stockActual, stockMinimo
- Relaciones: imagenes (1:N con ImagenProducto)

### ImagenProducto
- Campos: nombreArchivo, rutaArchivo, tipoMime, tamañoArchivo
- Relación N:1 con Producto
- Validaciones: solo imágenes (jpg, jpeg, png, gif, webp), máximo 5MB

### Carrito
- Relación 1:1 con Usuario
- Contiene ItemCarrito con reservas temporales

### Pedido
- Estados: PENDIENTE_PAGO, PAGADO, EN_DESPACHO, ENTREGADO, CANCELADO
- Contiene ItemPedido
- Soporte para ventas online y presenciales

### AuditoriaOperacion
- Registra todas las operaciones importantes
- Usuario, acción, entidad, fecha, detalles

## 🔐 Endpoints

### Autenticación (`/api/auth`)
- `POST /registro` - Registrar nuevo usuario
- `POST /login` - Iniciar sesión
- `POST /verificar-email` - Verificar email con token
- `POST /recuperar-password` - Solicitar recuperación
- `POST /restablecer-password` - Restablecer con token

### Productos (`/api/productos`)
- `GET /` - Listar todos los productos
- `GET /disponibles` - Productos con stock
- `GET /buscar?q={texto}` - Buscar productos
- `GET /{id}` - Obtener producto
- `POST /` - Crear producto (MANAGER/ADMIN)
- `PUT /{id}` - Actualizar producto (MANAGER/ADMIN)
- `PUT /{id}/stock` - Actualizar stock (MANAGER/ADMIN)
- `DELETE /{id}` - Eliminar producto (ADMIN)
- `GET /stock-bajo` - Productos con stock bajo (MANAGER/ADMIN)

### Imágenes de Productos (`/api/productos/{productoId}/imagenes`)
- `POST /` - Subir imagen (MANAGER/ADMIN)
- `POST /multiple` - Subir múltiples imágenes (MANAGER/ADMIN)
- `GET /{imagenId}` - Descargar/ver imagen
- `DELETE /{imagenId}` - Eliminar imagen (MANAGER/ADMIN)
- `DELETE /` - Eliminar todas las imágenes (MANAGER/ADMIN)

### Carrito (`/api/carrito`) - COMPRADOR
- `GET /` - Obtener mi carrito
- `POST /items` - Agregar producto
- `PUT /items/{itemId}` - Actualizar cantidad
- `DELETE /items/{itemId}` - Eliminar item
- `DELETE /` - Vaciar carrito

### Pedidos (`/api/pedidos`) - COMPRADOR
- `POST /` - Crear pedido desde carrito
- `GET /` - Obtener mis pedidos
- `GET /{pedidoId}` - Obtener pedido por ID
- `DELETE /{pedidoId}` - Cancelar pedido

### Manager (`/api/manager`) - MANAGER/ADMIN
- `GET /usuarios/compradores` - Listar compradores
- `GET /pedidos` - Todos los pedidos
- `GET /pedidos/estado/{estado}` - Pedidos por estado
- `POST /pedidos/{pedidoId}/confirmar-pago` - Confirmar pago
- `POST /pedidos/{pedidoId}/codigo-envio` - Asignar código de envío
- `POST /pedidos/{pedidoId}/marcar-entregado` - Marcar como entregado
- `POST /ventas-presenciales` - Registrar venta presencial

### Admin (`/api/admin`) - ADMIN
- `GET /usuarios` - Listar todos los usuarios
- `POST /usuarios` - Crear usuario
- `PUT /usuarios/{id}` - Actualizar usuario
- `DELETE /usuarios/{id}` - Eliminar usuario
- `PATCH /usuarios/{id}/estado` - Cambiar estado
- `GET /reportes/ventas` - Reporte de ventas
- `GET /auditoria` - Registros de auditoría
- `GET /productos/stock-bajo` - Productos con stock bajo

## ⚙️ Configuración

### Variables de Entorno (.env)
```properties
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/sistema_stock
DB_USS=postgres
DB_PASS=password

# JWT
JWT_SECRET=tu-clave-secreta-muy-larga-y-segura
JWT_EXPIRATION=86400000

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-password-de-aplicacion

# Frontend
FRONTEND_URL=http://localhost:3000

# Almacenamiento
FILE_STORAGE_PATH=./files
```

## 🔄 Tareas Programadas

### Limpieza de Reservas (cada hora)
- Elimina items del carrito con reservas expiradas
- Cancela pedidos pendientes después de 3 días
- Devuelve el stock al inventario

## 📧 Notificaciones por Email

- Confirmación de registro
- Confirmación de pedido
- Confirmación de pago
- Código de envío
- Recuperación de contraseña

## 🛡️ Seguridad

- Autenticación JWT
- Roles y permisos
- CORS configurado
- Passwords hasheados con BCrypt
- Tokens de verificación con expiración

## 🚀 Ejecución

```bash
# Compilar
./mvnw clean package

# Ejecutar
./mvnw spring-boot:run

# Compilar para producción
./mvnw clean package -DskipTests
```

## 📚 Documentación API

Accede a la documentación interactiva en:
- Scalar UI: `http://localhost:8080/scalar/api-docs`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

## 🧪 Testing

El sistema incluye validaciones en todos los endpoints y manejo de errores consistente.

## 📝 Mapeo de Historias de Usuario

### Comprador
✅ HU1-2: Registro e inicio de sesión
✅ HU3: Navegar catálogo de productos
✅ HU4-5: Agregar y gestionar carrito
✅ HU6: Realizar pedido
✅ HU7: Confirmación de pago y envío por email
✅ HU8: Historial de pedidos

### Manager
✅ HU9-10: Cargar y actualizar productos/stock
✅ HU11: Gestionar pedidos
✅ HU12: Registrar venta presencial
✅ HU13: Confirmar pago y cargar comprobante
✅ HU14: Generar código de envío

### Administrador
✅ HU15: Gestionar usuarios
✅ HU16: Reportes de ventas y stock
✅ HU17: Auditoría de operaciones

## 🔧 Próximas Mejoras

- ~~Upload de comprobantes de pago~~ ✅ (usar ImagenProductoService como base)
- ~~Upload de imágenes de productos~~ ✅ Implementado
- Exportación de reportes a PDF/Excel
- Paginación en listados
- Filtros avanzados
- Notificaciones push
- Integración con pasarelas de pago
- Optimización de imágenes (thumbnails, compresión)
- Sistema de categorías de productos
