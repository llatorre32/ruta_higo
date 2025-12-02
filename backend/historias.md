# Historias de Usuario - Sistema de Stock y Ventas Web

## 🧍 Rol: Comprador

### 1. Registro de usuario
**Como** comprador  
**Quiero** registrarme en el sistema con usuario, contraseña y correo electrónico  
**Para** poder acceder a mi cuenta y realizar compras online.  

**Criterios de aceptación:**
- Debo poder registrarme ingresando nombre, email y contraseña.  
- El sistema valida que el email no esté registrado.  
- Se envía un correo de confirmación de registro.  
- El usuario queda activo una vez confirma su correo.  

**Prioridad:** Alta  

---

### 2. Inicio de sesión
**Como** comprador  
**Quiero** iniciar sesión con mi usuario y contraseña  
**Para** acceder a mi perfil y al carrito de compras.  

**Criterios de aceptación:**
- El sistema valida credenciales.  
- Si el login es exitoso, redirige al catálogo de productos.  
- Si no, muestra mensaje de error.  

**Prioridad:** Alta  

---

### 3. Navegar catálogo de productos
**Como** comprador  
**Quiero** ver el catálogo de productos disponibles  
**Para** poder elegir qué productos comprar.  

**Criterios de aceptación:**
- Debo poder ver nombre, imagen, descripción, precio y stock disponible.  
- Puedo filtrar y buscar productos.  
- Los productos sin stock deben aparecer como “agotados”.  

**Prioridad:** Alta  

---

### 4. Agregar productos al carrito
**Como** comprador  
**Quiero** agregar productos al carrito  
**Para** preparar mi pedido antes de pagar.  

**Criterios de aceptación:**
- Puedo agregar uno o varios productos al carrito.  
- Al agregar un producto, el stock se “reserva” por 3 días.  
- Si no completo la compra en ese plazo, la reserva se cancela.  

**Prioridad:** Alta  

---

### 5. Gestionar carrito
**Como** comprador  
**Quiero** ver, modificar o eliminar productos de mi carrito  
**Para** ajustar mi pedido antes de finalizar la compra.  

**Criterios de aceptación:**
- Puedo cambiar la cantidad o eliminar productos del carrito.  
- El sistema recalcula el total automáticamente.  
- El stock reservado se ajusta según los cambios.  

**Prioridad:** Alta  

---

### 6. Realizar pedido
**Como** comprador  
**Quiero** enviar mi pedido para confirmarlo  
**Para** que el sistema gestione el pago y envío.  

**Criterios de aceptación:**
- Al confirmar el pedido, se genera una orden en estado “Pendiente de pago”.  
- Se envía un correo con instrucciones de pago.  
- El stock del pedido queda reservado por 3 días.  

**Prioridad:** Alta  

---

### 7. Recibir confirmación de pago y envío
**Como** comprador  
**Quiero** recibir por email la confirmación del pago y el código de envío  
**Para** poder hacer seguimiento de mi pedido.  

**Criterios de aceptación:**
- Una vez el manager confirma el pago, recibo un correo de confirmación.  
- Al despacharse el pedido, recibo el código de envío y comprobante.  

**Prioridad:** Media  

---

### 8. Historial de pedidos
**Como** comprador  
**Quiero** ver el historial de mis pedidos y su estado  
**Para** saber qué compras hice y su situación actual.  

**Criterios de aceptación:**
- Puedo ver lista de pedidos con fechas, montos y estado.  
- Estados posibles: “Pendiente de pago”, “Pagado”, “En despacho”, “Entregado”, “Cancelado”.  

**Prioridad:** Media  

---

## 🧰 Rol: Manager

### 9. Cargar nuevos productos
**Como** manager  
**Quiero** poder agregar nuevos productos al sistema  
**Para** mantener actualizado el catálogo.  

**Criterios de aceptación:**
- Puedo crear, editar y eliminar productos.  
- Puedo cargar imágenes, precios y stock disponible.  
- El sistema valida que el nombre del producto no se repita.  

**Prioridad:** Alta  

---

### 10. Actualizar stock
**Como** manager  
**Quiero** actualizar el stock manualmente  
**Para** reflejar correctamente el inventario físico.  

**Criterios de aceptación:**
- Puedo modificar la cantidad disponible.  
- Los cambios se reflejan inmediatamente en el catálogo.  

**Prioridad:** Alta  

---

### 11. Gestionar pedidos
**Como** manager  
**Quiero** ver todos los pedidos pendientes  
**Para** confirmar pagos y preparar los envíos.  

**Criterios de aceptación:**
- Puedo filtrar por estado (“Pendiente de pago”, “Pagado”, “En despacho”).  
- Al confirmar el pago, el estado cambia a “Pagado”.  
- Al preparar el envío, puedo agregar el código de despacho.  
- Se envía notificación automática al comprador.  

**Prioridad:** Alta  

---

### 12. Registrar venta en local
**Como** manager  
**Quiero** registrar una compra presencial en el sistema  
**Para** mantener actualizado el stock y las estadísticas de ventas.  

**Criterios de aceptación:**
- Puedo crear un pedido sin registro de usuario.  
- El stock se descuenta automáticamente.  
- Puedo generar comprobante de venta.  

**Prioridad:** Media  

---

### 13. Confirmar pago y cargar comprobante
**Como** manager  
**Quiero** confirmar los pagos recibidos y adjuntar comprobantes  
**Para** validar la compra antes del despacho.  

**Criterios de aceptación:**
- Puedo subir comprobantes (PDF o imagen).  
- El pedido cambia a estado “Pagado”.  
- El comprador recibe confirmación por email.  

**Prioridad:** Alta  

---

### 14. Generar código de envío
**Como** manager  
**Quiero** asignar un código de envío al pedido  
**Para** poder rastrear el despacho.  

**Criterios de aceptación:**
- El sistema guarda el código junto al pedido.  
- El comprador recibe el código por email.  

**Prioridad:** Media  

---

## 🧑‍💼 Rol: Administrador

### 15. Gestionar usuarios
**Como** administrador  
**Quiero** ver y gestionar los usuarios del sistema  
**Para** mantener el control de accesos y roles.  

**Criterios de aceptación:**
- Puedo listar, activar, desactivar o eliminar usuarios.  
- Puedo cambiar roles (comprador, manager, admin).  

**Prioridad:** Alta  

---

### 16. Ver reportes de ventas y stock
**Como** administrador  
**Quiero** acceder a reportes detallados de ventas y stock  
**Para** analizar el rendimiento del negocio.  

**Criterios de aceptación:**
- Puedo ver reportes por rango de fechas, categoría o vendedor.  
- Puedo exportar reportes a Excel o PDF.  
- Se muestran métricas como ingresos, pedidos, productos más vendidos, y stock bajo.  

**Prioridad:** Alta  

---

### 17. Auditoría de operaciones
**Como** administrador  
**Quiero** registrar todas las acciones importantes del sistema  
**Para** garantizar trazabilidad y seguridad.  

**Criterios de aceptación:**
- Se registra fecha, usuario y acción (alta, baja, modificación, confirmación de pago, etc.).  
- Los registros pueden consultarse por usuario o rango de fechas.  

**Prioridad:** Media  
