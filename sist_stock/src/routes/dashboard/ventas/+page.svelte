<script>
  import { onMount, onDestroy } from "svelte";
  import { notify } from '$lib/stores/notifications.js';
  import { setupAutoRefresh, cleanupAutoRefresh } from '$lib/utils/autoRefresh.js';

  export let data;
  let ventas = data.ventas || [];
  
  // Setup auto-refresh
  onMount(async () => {
    await cargarProductos();
    await cargarClientes();
    setupAutoRefresh('ventas');
  });

  onDestroy(() => {
    cleanupAutoRefresh('ventas');
  });
  let productos = [];
  let clientes = [];

  // Variables del formulario de venta
  let mostrarFormulario = false;
  let clienteSeleccionado = null;
  let tipoCliente = "existente"; // 'existente' o 'nuevo'
  let clienteNombre = "";
  let clienteDocumento = "";
  let clienteTelefono = "";
  let clienteEmail = "";
  let clienteDireccion = "";
  let metodoPago = "efectivo";
  let productosVenta = [];
  let subtotal = 0;
  let descuento = 0;
  let impuestos = 0;
  let total = 0;

  // Variables de búsqueda de productos
  let busquedaProducto = "";
  let productosFiltrados = [];

  // Variables del modal de email
  let mostrarModalEmail = false;
  let ventaGuardada = null;
  let emailCliente = "";
  let asuntoEmail = "";
  let mensajeEmail = "";
  let enviandoEmail = false;

  async function cargarProductos() {
    try {
      const response = await fetch("/api/productos");
      if (response.ok) {
        productos = await response.json();
      }
    } catch (error) {
      console.error("Error al cargar productos:", error);
    }
  }

  async function cargarClientes() {
    try {
      const response = await fetch("/api/clientes");
      if (response.ok) {
        clientes = await response.json();
      }
    } catch (error) {
      console.error("Error al cargar clientes:", error);
    }
  }

  function buscarProductos() {
    if (busquedaProducto.length < 2) {
      productosFiltrados = [];
      return;
    }

    productosFiltrados = productos.filter(
      (producto) =>
        producto.nombre
          .toLowerCase()
          .includes(busquedaProducto.toLowerCase()) ||
        producto.codigo.toLowerCase().includes(busquedaProducto.toLowerCase()),
    );
  }

  function seleccionarCliente(cliente) {
    clienteSeleccionado = cliente;
    clienteNombre = cliente.nombre;
    clienteDocumento = cliente.documento || "";
    clienteTelefono = cliente.telefono || "";
    clienteEmail = cliente.email || "";
    clienteDireccion = cliente.direccion || "";
  }

  function cambiarTipoCliente() {
    if (tipoCliente === "nuevo") {
      limpiarDatosCliente();
    }
  }

  function limpiarDatosCliente() {
    clienteSeleccionado = null;
    clienteNombre = "";
    clienteDocumento = "";
    clienteTelefono = "";
    clienteEmail = "";
    clienteDireccion = "";
  }

  function agregarProducto(producto) {
    const existente = productosVenta.find((p) => p.id === producto.id);

    if (existente) {
      existente.cantidad += 1;
    } else {
      productosVenta.push({
        id: producto.id,
        nombre: producto.nombre,
        codigo: producto.codigo,
        precioUnitario: producto.precio_venta || 0,
        cantidad: 1,
        stockDisponible: producto.stock_actual,
      });
    }

    productosVenta = [...productosVenta];
    calcularTotales();
    busquedaProducto = "";
    productosFiltrados = [];
  }

  function removerProducto(index) {
    productosVenta.splice(index, 1);
    productosVenta = [...productosVenta];
    calcularTotales();
  }

  function actualizarCantidad(index, cantidad) {
    if (cantidad <= 0) {
      removerProducto(index);
      return;
    }

    productosVenta[index].cantidad = cantidad;
    productosVenta = [...productosVenta];
    calcularTotales();
  }

  function calcularTotales() {
    subtotal = productosVenta.reduce(
      (sum, item) => sum + item.cantidad * item.precioUnitario,
      0,
    );
    total = subtotal - descuento + impuestos;
  }

  async function guardarVenta() {
    // Validaciones
    if (!clienteNombre.trim()) {
      notify.error("El nombre del cliente es obligatorio");
      return;
    }

    if (productosVenta.length === 0) {
      notify.error("Debe agregar al menos un producto");
      return;
    }

    // Validar stock antes de enviar
    const stockInsuficiente = productosVenta.find(item => item.cantidad > item.stockDisponible);
    if (stockInsuficiente) {
      notify.error(`Stock insuficiente para ${stockInsuficiente.nombre}. Disponible: ${stockInsuficiente.stockDisponible}`);
      return;
    }

    try {
      // Si es un cliente nuevo, primero lo creamos
      if (tipoCliente === "nuevo") {
        const responseCliente = await fetch("/api/clientes", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            nombre: clienteNombre,
            documento: clienteDocumento,
            telefono: clienteTelefono,
            email: clienteEmail,
            direccion: clienteDireccion,
          }),
        });

        if (!responseCliente.ok) {
          const error = await responseCliente.json();
          notify.error("Error al crear cliente: " + error.error);
          return;
        }
      }

      // Crear la venta
      const ventaData = {
        clienteNombre,
        clienteDocumento,
        clienteTelefono,
        clienteEmail,
        productos: productosVenta,
        subtotal,
        descuento,
        impuestos,
        total,
        metodoPago,
      };
      
      console.log('Enviando datos de venta:', ventaData);

      const response = await fetch("/api/ventas", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(ventaData),
      });

      if (response.ok) {
        const ventaCreada = await response.json();
        console.log('Venta creada:', ventaCreada);
        
        ventaGuardada = ventaCreada;
        emailCliente = clienteEmail || "";
        asuntoEmail = `Comprobante de Venta #${ventaCreada.numero_venta || "Sin número"}`;
        mensajeEmail = generarMensajeEmail(ventaCreada);

        limpiarFormulario();
        mostrarModalEmail = true;
        notify.success(`Venta creada correctamente - #${ventaCreada.numero_venta || "Sin número"}`);
      } else {
        const error = await response.json();
        console.error('Error del servidor:', error);
        notify.error("Error: " + error.error);
      }
    } catch (error) {
      console.error("Error al guardar venta:", error);
      notify.error("Error al guardar la venta");
    }
  }

  function resetearFormulario() {
    // Resetear todas las variables del formulario
    mostrarFormulario = false;
    clienteSeleccionado = null;
    tipoCliente = "existente";
    clienteNombre = "";
    clienteDocumento = "";
    clienteTelefono = "";
    clienteEmail = "";
    clienteDireccion = "";
    metodoPago = "efectivo";
    productosVenta = [];
    subtotal = 0;
    descuento = 0;
    impuestos = 0;
    total = 0;
    busquedaProducto = "";
    productosFiltrados = [];
  }

  function limpiarFormulario() {
    resetearFormulario();
  }

  async function cancelarVenta(ventaId) {
    if (
      !confirm(
        "¿Está seguro de cancelar esta venta? Esta acción no se puede deshacer.",
      )
    ) {
      return;
    }

    try {
      const response = await fetch(`/api/ventas/${ventaId}`, {
        method: "DELETE",
      });

      if (response.ok) {
        notify.success("Venta cancelada correctamente");
        location.reload();
      } else {
        const error = await response.json();
        notify.error("Error: " + error.error);
      }
    } catch (error) {
      console.error("Error al cancelar venta:", error);
      notify.error("Error al cancelar la venta");
    }
  }

  function formatCurrency(amount) {
    return new Intl.NumberFormat("es-AR", {
      style: "currency",
      currency: "ARS",
    }).format(amount);
  }

  function generarMensajeEmail(venta) {
    // Usar los datos de la venta guardada o valores por defecto
    const numeroVenta = venta?.numero_venta || 'Sin asignar';
    const fechaVenta = new Date().toLocaleDateString("es-AR");
    const metodoPagoTexto = metodoPago.charAt(0).toUpperCase() + metodoPago.slice(1);
    
    const productosTexto = productosVenta.map((item) => 
      `- ${item.nombre} x${item.cantidad} - ${formatCurrency(item.cantidad * item.precioUnitario)}`
    ).join("\n");
    
    const subtotalTexto = formatCurrency(subtotal);
    const descuentoTexto = descuento > 0 ? `\nDescuento: -${formatCurrency(descuento)}` : '';
    const impuestosTexto = impuestos > 0 ? `\nImpuestos: ${formatCurrency(impuestos)}` : '';
    const totalTexto = formatCurrency(total);

    return `Estimado/a ${clienteNombre},

Gracias por su compra. A continuación encontrará los detalles de su venta:

Número de Venta: ${numeroVenta}
Fecha: ${fechaVenta}
Método de Pago: ${metodoPagoTexto}

Productos adquiridos:
${productosTexto}

Subtotal: ${subtotalTexto}${descuentoTexto}${impuestosTexto}
Total: ${totalTexto}

¡Muchas gracias por confiar en nosotros!

Saludos cordiales.`;
  }

  async function enviarEmail() {
    if (!emailCliente.trim()) {
      notify.error("Debe ingresar un email válido");
      return;
    }

    if (!asuntoEmail.trim() || !mensajeEmail.trim()) {
      notify.error("El asunto y mensaje son obligatorios");
      return;
    }

    enviandoEmail = true;

    try {
      const response = await fetch("/api/email", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          para: emailCliente,
          asunto: asuntoEmail,
          mensaje: mensajeEmail,
          ventaId: ventaGuardada?.id,
        }),
      });

      if (response.ok) {
        notify.success("Email enviado correctamente");
        cerrarModalEmail();
      } else {
        const error = await response.json();
        notify.error("Error al enviar email: " + (error.error || "Error desconocido"));
      }
    } catch (error) {
      console.error("Error al enviar email:", error);
      notify.error("Error de conexión al enviar email");
    } finally {
      enviandoEmail = false;
    }
  }

  function cerrarModalEmail() {
    mostrarModalEmail = false;
    ventaGuardada = null;
    emailCliente = "";
    asuntoEmail = "";
    mensajeEmail = "";
    location.reload();
  }

  function saltarEmail() {
    cerrarModalEmail();
  }
</script>

<svelte:head>
  <title>Ventas - Sistema de Stock</title>
</svelte:head>

<div class="page-header">
  <h1>Gestión de Ventas</h1>
  <button class="btn-primary" on:click={() => (mostrarFormulario = true)}>
    ➕ Nueva Venta
  </button>
</div>

<!-- Formulario de Nueva Venta -->
{#if mostrarFormulario}
  <div class="modal-overlay">
    <div class="modal-sales">
      <div class="modal-header">
        <h2>Nueva Venta</h2>
        <button class="btn-close" on:click={limpiarFormulario}>×</button>
      </div>

      <div class="modal-content">
        <!-- Selección de Tipo de Cliente -->
        <div class="step-section">
          <h3 class="step-title">
            <span class="step-number">1</span>
            Seleccionar Cliente
          </h3>

          <div class="client-type-selector">
            <label class="radio-option">
              <input
                type="radio"
                bind:group={tipoCliente}
                value="existente"
                on:change={cambiarTipoCliente}
              />
              <span class="radio-custom"></span>
              Cliente Existente
            </label>
            <label class="radio-option">
              <input
                type="radio"
                bind:group={tipoCliente}
                value="nuevo"
                on:change={cambiarTipoCliente}
              />
              <span class="radio-custom"></span>
              Nuevo Cliente
            </label>
          </div>

          {#if tipoCliente === "existente"}
            <div class="form-group">
              <label for="clienteSelect">Seleccionar Cliente</label>
              <select
                id="clienteSelect"
                bind:value={clienteSeleccionado}
                on:change={() =>
                  clienteSeleccionado &&
                  seleccionarCliente(clienteSeleccionado)}
              >
                <option value={null}>-- Seleccione un cliente --</option>
                {#each clientes as cliente}
                  <option value={cliente}>
                    {cliente.nombre}
                    {cliente.documento ? `(${cliente.documento})` : ""}
                  </option>
                {/each}
              </select>
            </div>

            {#if clienteSeleccionado}
              <div class="selected-client-info">
                <h4>Cliente Seleccionado:</h4>
                <div class="client-details">
                  <p><strong>Nombre:</strong> {clienteSeleccionado.nombre}</p>
                  {#if clienteSeleccionado.documento}
                    <p>
                      <strong>Documento:</strong>
                      {clienteSeleccionado.documento}
                    </p>
                  {/if}
                  {#if clienteSeleccionado.telefono}
                    <p>
                      <strong>Teléfono:</strong>
                      {clienteSeleccionado.telefono}
                    </p>
                  {/if}
                  {#if clienteSeleccionado.email}
                    <p><strong>Email:</strong> {clienteSeleccionado.email}</p>
                  {/if}
                </div>
              </div>
            {/if}
          {/if}
        </div>

        <!-- Datos del Cliente (editables si es nuevo cliente o cliente existente) -->
        {#if tipoCliente === "nuevo" || clienteSeleccionado}
          <div class="step-section">
            <h3 class="step-title">
              <span class="step-number">2</span>
              {tipoCliente === "nuevo"
                ? "Datos del Nuevo Cliente"
                : "Confirmar Datos del Cliente"}
            </h3>

            <div class="form-grid">
              <div class="form-group">
                <label for="clienteNombre">Nombre del Cliente *</label>
                <input
                  type="text"
                  id="clienteNombre"
                  bind:value={clienteNombre}
                  required
                />
              </div>
              <div class="form-group">
                <label for="clienteDocumento">Documento</label>
                <input
                  type="text"
                  id="clienteDocumento"
                  bind:value={clienteDocumento}
                />
              </div>
              <div class="form-group">
                <label for="clienteTelefono">Teléfono</label>
                <input
                  type="text"
                  id="clienteTelefono"
                  bind:value={clienteTelefono}
                />
              </div>
              <div class="form-group">
                <label for="clienteEmail">Email</label>
                <input
                  type="email"
                  id="clienteEmail"
                  bind:value={clienteEmail}
                />
              </div>
              {#if tipoCliente === "nuevo"}
                <div class="form-group full-width">
                  <label for="clienteDireccion">Dirección</label>
                  <input
                    type="text"
                    id="clienteDireccion"
                    bind:value={clienteDireccion}
                  />
                </div>
              {/if}
            </div>
          </div>
        {/if}

        <!-- Búsqueda de Productos -->
        {#if (tipoCliente === "existente" && clienteSeleccionado) || (tipoCliente === "nuevo" && clienteNombre.trim())}
          <div class="step-section">
            <h3 class="step-title">
              <span class="step-number">3</span>
              Agregar Productos
            </h3>

            <div class="product-search">
              <div class="form-group">
                <label for="busquedaProducto">Buscar Producto</label>
                <input
                  type="text"
                  id="busquedaProducto"
                  bind:value={busquedaProducto}
                  on:input={buscarProductos}
                  placeholder="Escriba el nombre o código del producto..."
                />
                {#if productosFiltrados.length > 0}
                  <div class="search-results">
                    {#each productosFiltrados as producto}
                      <button
                        type="button"
                        class="search-result-item"
                        on:click={() => agregarProducto(producto)}
                      >
                        <div class="product-name">{producto.nombre}</div>
                        <div class="product-details">
                          Código: {producto.codigo} | Stock: {producto.stock_actual}
                          | Precio: {formatCurrency(producto.precio_venta || 0)}
                        </div>
                      </button>
                    {/each}
                  </div>
                {/if}
              </div>
            </div>
          </div>

          <!-- Lista de Productos en la Venta -->
          {#if productosVenta.length > 0}
            <div class="step-section">
              <h3 class="step-title">
                <span class="step-number">4</span>
                Productos en la Venta
              </h3>

              <div class="products-table-container">
                <table class="products-table">
                  <thead>
                    <tr>
                      <th>Producto</th>
                      <th>Precio</th>
                      <th>Stock Disponible</th>
                      <th>Cantidad</th>
                      <th>Subtotal</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {#each productosVenta as item, index}
                      <tr>
                        <td>
                          <div class="product-info">
                            <div class="product-name">{item.nombre}</div>
                            <div class="product-code">{item.codigo}</div>
                          </div>
                        </td>
                        <td class="price-cell">
                          {formatCurrency(item.precioUnitario)}
                        </td>
                        <td class="stock-cell">
                          <span class="stock-amount {item.stockDisponible <= 5 ? 'stock-low' : item.stockDisponible <= 10 ? 'stock-medium' : 'stock-high'}">
                            {item.stockDisponible}
                          </span>
                          <span class="stock-label">unidades</span>
                        </td>
                        <td>
                          <input
                            type="number"
                            min="1"
                            max={item.stockDisponible}
                            value={item.cantidad}
                            on:input={(e) =>
                              actualizarCantidad(
                                index,
                                parseInt(e.target.value),
                              )}
                            class="quantity-input"
                          />
                        </td>
                        <td class="price-cell">
                          {formatCurrency(item.cantidad * item.precioUnitario)}
                        </td>
                        <td>
                          <button
                            type="button"
                            class="btn-remove"
                            on:click={() => removerProducto(index)}
                          >
                            🗑️
                          </button>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Totales y Finalización -->
            <div class="step-section">
              <div class="totals-grid">
                <div class="payment-section">
                  <h3 class="step-title">
                    <span class="step-number">5</span>
                    Método de Pago
                  </h3>
                  <div class="form-group">
                    <select bind:value={metodoPago}>
                      <option value="efectivo">Efectivo</option>
                      <option value="tarjeta">Tarjeta</option>
                      <option value="transferencia">Transferencia</option>
                    </select>
                  </div>
                </div>

                <div class="totals-section">
                  <h3 class="step-title">
                    <span class="step-number">6</span>
                    Totales
                  </h3>

                  <div class="totals-content">
                    <div class="total-row">
                      <span>Subtotal:</span>
                      <span class="amount">{formatCurrency(subtotal)}</span>
                    </div>
                    <div class="total-row">
                      <span>Descuento:</span>
                      <input
                        type="number"
                        bind:value={descuento}
                        on:input={calcularTotales}
                        step="0.01"
                        class="amount-input"
                      />
                    </div>
                    <div class="total-row">
                      <span>Impuestos:</span>
                      <input
                        type="number"
                        bind:value={impuestos}
                        on:input={calcularTotales}
                        step="0.01"
                        class="amount-input"
                      />
                    </div>
                    <div class="total-row total-final">
                      <span>Total:</span>
                      <span class="amount total-amount"
                        >{formatCurrency(total)}</span
                      >
                    </div>
                  </div>
                </div>
              </div>
            </div>
          {/if}
        {:else}
          <div class="warning-message">
            <p>
              {#if tipoCliente === "existente"}
                Por favor, seleccione un cliente existente para continuar.
              {:else}
                Por favor, ingrese al menos el nombre del cliente para
                continuar.
              {/if}
            </p>
          </div>
        {/if}
      </div>

      <!-- Botones -->
      <div class="modal-footer">
        <button type="button" class="btn-cancel" on:click={limpiarFormulario}>
          Cancelar
        </button>
        {#if ((tipoCliente === "existente" && clienteSeleccionado) || (tipoCliente === "nuevo" && clienteNombre.trim())) && productosVenta.length > 0}
          <button type="button" class="btn-save" on:click={guardarVenta}>
            Guardar Venta
          </button>
        {:else}
          <button
            type="button"
            disabled
            class="btn-save-disabled"
            title="Complete la selección del cliente y agregue productos para continuar"
          >
            Guardar Venta
          </button>
        {/if}
      </div>
    </div>
  </div>
{/if}

<!-- Modal de Email -->
{#if mostrarModalEmail}
  <div class="modal-overlay">
    <div class="modal-email">
      <div class="modal-header">
        <h2>Enviar Comprobante por Email</h2>
        <button class="btn-close" on:click={cerrarModalEmail}>×</button>
      </div>

      <div class="modal-content">
        <div class="email-info">
          <p class="success-message">
            ✅ Venta registrada correctamente
            {#if ventaGuardada?.numero_venta}
              - Número: <strong>{ventaGuardada.numero_venta}</strong>
            {/if}
          </p>
          <p class="email-subtitle">
            ¿Desea enviar el comprobante por email al cliente?
          </p>
        </div>

        <form on:submit|preventDefault={enviarEmail}>
          <div class="form-group">
            <label for="emailCliente">Email del Cliente *</label>
            <input
              type="email"
              id="emailCliente"
              bind:value={emailCliente}
              required
              disabled={enviandoEmail}
              placeholder="cliente@ejemplo.com"
            />
          </div>

          <div class="form-group">
            <label for="asuntoEmail">Asunto *</label>
            <input
              type="text"
              id="asuntoEmail"
              bind:value={asuntoEmail}
              required
              disabled={enviandoEmail}
              placeholder="Asunto del email"
            />
          </div>

          <div class="form-group">
            <label for="mensajeEmail">Mensaje *</label>
            <textarea
              id="mensajeEmail"
              bind:value={mensajeEmail}
              required
              disabled={enviandoEmail}
              placeholder="Mensaje del email"
              rows="10"
            ></textarea>
          </div>
        </form>
      </div>

      <div class="modal-footer">
        <button
          type="button"
          class="btn-skip"
          on:click={saltarEmail}
          disabled={enviandoEmail}
        >
          Saltar
        </button>
        <button
          type="button"
          class="btn-send-email"
          on:click={enviarEmail}
          disabled={enviandoEmail || !emailCliente.trim()}
        >
          {enviandoEmail ? "Enviando..." : "📧 Enviar Email"}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- Lista de Ventas -->
<div class="table-container">
  <table class="sales-table">
    <thead>
      <tr>
        <th>Número</th>
        <th>Cliente</th>
        <th>Total</th>
        <th>Método</th>
        <th>Estado</th>
        <th>Fecha</th>
        <th>Acciones</th>
      </tr>
    </thead>
    <tbody>
      {#each ventas as venta}
        <tr>
          <td>
            <code class="sale-number">{venta.numero_venta}</code>
          </td>
          <td>
            <div class="client-info">
              {venta.cliente_nombre}
            </div>
          </td>
          <td class="price-cell">
            {formatCurrency(venta.total)}
          </td>
          <td>
            <span class="payment-method">{venta.metodo_pago}</span>
          </td>
          <td>
            <span class="status-badge {venta.estado}">
              {venta.estado === "completada" ? "Completada" : "Cancelada"}
            </span>
          </td>
          <td class="date-cell">
            {new Date(venta.created_at).toLocaleDateString()}
          </td>
          <td>
            <div class="actions">
              {#if venta.estado === "completada"}
                <button
                  class="btn-cancel-sale"
                  on:click={() => cancelarVenta(venta.id)}
                >
                  Cancelar
                </button>
              {/if}
              <a href="/dashboard/ventas/{venta.id}" class="btn-view">
                Ver Detalle
              </a>
            </div>
          </td>
        </tr>
      {:else}
        <tr>
          <td colspan="7" class="empty-state">
            No se encontraron ventas registradas
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>

<style>
  /* Page Layout */
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
  }

  .page-header h1 {
    margin: 0;
    color: #333;
    font-size: 2rem;
  }

  /* Table Styles */
  .table-container {
    background: white;
    border-radius: 10px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  .sales-table {
    width: 100%;
    border-collapse: collapse;
  }

  .sales-table th {
    background-color: #f8f9fa;
    padding: 1rem;
    text-align: left;
    font-weight: 600;
    color: #333;
    border-bottom: 2px solid #dee2e6;
  }

  .sales-table td {
    padding: 1rem;
    border-bottom: 1px solid #dee2e6;
    vertical-align: middle;
  }

  .sales-table tr:hover {
    background-color: #f8f9fa;
  }

  /* Button Styles */
  .btn-primary {
    background: #667eea;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 5px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .btn-primary:hover {
    background: #5a67d8;
  }

  /* Modal Styles */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    padding: 1rem;
  }

  .modal-sales {
    background: white;
    border-radius: 10px;
    width: 100%;
    max-width: 900px;
    max-height: 90vh;
    overflow: hidden;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 2px solid #f0f0f0;
    background-color: #f8f9fa;
  }

  .modal-header h2 {
    margin: 0;
    color: #333;
    font-size: 1.5rem;
  }

  .btn-close {
    background: none;
    border: none;
    font-size: 2rem;
    cursor: pointer;
    color: #666;
    padding: 0;
    line-height: 1;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color 0.2s;
  }

  .btn-close:hover {
    background-color: #f0f0f0;
  }

  .modal-content {
    max-height: calc(90vh - 180px);
    overflow-y: auto;
    padding: 1.5rem;
    flex: 1;
  }

  /* Step Sections */
  .step-section {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 1.5rem;
    border: 1px solid #e9ecef;
  }

  .step-title {
    display: flex;
    align-items: center;
    margin: 0 0 1rem 0;
    font-size: 1.1rem;
    font-weight: 600;
    color: #333;
  }

  .step-number {
    background: #667eea;
    color: white;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.9rem;
    margin-right: 0.75rem;
    font-weight: 600;
  }

  /* Client Type Selector */
  .client-type-selector {
    display: flex;
    gap: 1.5rem;
    margin-bottom: 1rem;
  }

  .radio-option {
    display: flex;
    align-items: center;
    cursor: pointer;
    font-weight: 500;
  }

  .radio-option input[type="radio"] {
    display: none;
  }

  .radio-custom {
    width: 20px;
    height: 20px;
    border: 2px solid #ddd;
    border-radius: 50%;
    margin-right: 0.5rem;
    position: relative;
    transition: border-color 0.2s;
  }

  .radio-option input[type="radio"]:checked + .radio-custom {
    border-color: #667eea;
  }

  .radio-option input[type="radio"]:checked + .radio-custom::after {
    content: "";
    width: 10px;
    height: 10px;
    background: #667eea;
    border-radius: 50%;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  /* Form Styles */
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }

  .form-group {
    display: flex;
    flex-direction: column;
  }

  .form-group.full-width {
    grid-column: 1 / -1;
  }

  .form-group label {
    margin-bottom: 0.5rem;
    font-weight: 500;
    color: #333;
    font-size: 0.9rem;
  }

  .form-group input,
  .form-group select,
  .form-group textarea {
    padding: 0.75rem;
    border: 2px solid #e2e8f0;
    border-radius: 5px;
    font-size: 1rem;
    transition: border-color 0.3s;
  }

  .form-group input:focus,
  .form-group select:focus,
  .form-group textarea:focus {
    outline: none;
    border-color: #667eea;
  }

  /* Selected Client Info */
  .selected-client-info {
    background: #e6f3ff;
    border: 1px solid #b3d9ff;
    border-radius: 6px;
    padding: 1rem;
    margin-top: 1rem;
  }

  .selected-client-info h4 {
    margin: 0 0 0.5rem 0;
    color: #0066cc;
    font-size: 0.9rem;
    font-weight: 600;
  }

  .client-details p {
    margin: 0.25rem 0;
    font-size: 0.85rem;
    color: #0066cc;
  }

  /* Product Search */
  .product-search {
    position: relative;
  }

  .search-results {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    border: 1px solid #ddd;
    border-radius: 5px;
    max-height: 300px;
    overflow-y: auto;
    z-index: 10;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .search-result-item {
    width: 100%;
    padding: 0.75rem;
    border: none;
    background: white;
    text-align: left;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    transition: background-color 0.2s;
  }

  .search-result-item:hover {
    background: #f8f9fa;
  }

  .search-result-item:last-child {
    border-bottom: none;
  }

  .search-result-item .product-name {
    font-weight: 500;
    color: #333;
    margin-bottom: 0.25rem;
  }

  .search-result-item .product-details {
    font-size: 0.85rem;
    color: #666;
  }

  /* Products Table */
  .products-table-container {
    background: white;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e9ecef;
  }

  .products-table {
    width: 100%;
    border-collapse: collapse;
  }

  .products-table th {
    background: #f8f9fa;
    padding: 0.75rem;
    text-align: left;
    font-weight: 600;
    color: #333;
    font-size: 0.9rem;
    border-bottom: 1px solid #dee2e6;
  }

  .products-table td {
    padding: 0.75rem;
    border-bottom: 1px solid #f0f0f0;
    vertical-align: middle;
  }

  .products-table tr:hover {
    background: #f8f9fa;
  }

  .product-info .product-name {
    font-weight: 500;
    color: #333;
    margin-bottom: 0.25rem;
  }

  .product-info .product-code {
    font-size: 0.85rem;
    color: #666;
    font-family: monospace;
  }

  .price-cell {
    font-weight: 500;
    color: #333;
    text-align: right;
  }

  .stock-cell {
    text-align: center;
    font-weight: 500;
  }

  .stock-amount {
    display: inline-block;
    padding: 0.25rem 0.5rem;
    border-radius: 12px;
    font-size: 0.9rem;
    font-weight: 600;
    margin-right: 0.25rem;
  }

  .stock-amount.stock-low {
    background: #fee2e2;
    color: #dc2626;
  }

  .stock-amount.stock-medium {
    background: #fef3c7;
    color: #d97706;
  }

  .stock-amount.stock-high {
    background: #d1fae5;
    color: #059669;
  }

  .stock-label {
    font-size: 0.8rem;
    color: #666;
    font-weight: normal;
  }

  .quantity-input {
    width: 70px;
    padding: 0.4rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    text-align: center;
    font-weight: 500;
  }

  .btn-remove {
    background: #fee2e2;
    border: none;
    padding: 0.5rem;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .btn-remove:hover {
    background: #fecaca;
  }

  /* Totals Grid */
  .totals-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
  }

  .payment-section,
  .totals-section {
    background: white;
    padding: 1.5rem;
    border-radius: 8px;
    border: 1px solid #e9ecef;
  }

  .totals-content {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .total-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 0.95rem;
  }

  .total-final {
    border-top: 2px solid #dee2e6;
    padding-top: 0.75rem;
    font-weight: 600;
    font-size: 1.1rem;
  }

  .amount {
    font-weight: 500;
    color: #333;
  }

  .total-amount {
    color: #667eea;
    font-size: 1.2rem;
  }

  .amount-input {
    width: 80px;
    padding: 0.4rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    text-align: right;
    font-weight: 500;
  }

  /* Warning Message */
  .warning-message {
    background: #fff3cd;
    border: 1px solid #ffeaa7;
    border-radius: 6px;
    padding: 1rem;
    margin: 1.5rem 0;
  }

  .warning-message p {
    margin: 0;
    color: #856404;
    font-weight: 500;
  }

  /* Modal Footer */
  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    padding: 1.5rem;
    border-top: 2px solid #f0f0f0;
    background-color: #f8f9fa;
    flex-shrink: 0;
    position: sticky;
    bottom: 0;
    z-index: 10;
  }

  .btn-cancel,
  .btn-save,
  .btn-save-disabled {
    padding: 0.75rem 1.5rem;
    border-radius: 5px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
    border: none;
  }

  .btn-cancel {
    background: #e2e8f0;
    color: #4a5568;
  }

  .btn-cancel:hover {
    background: #cbd5e0;
  }

  .btn-save {
    background: #48bb78;
    color: white;
  }

  .btn-save:hover {
    background: #38a169;
  }

  .btn-save-disabled {
    background: #a0aec0;
    color: white;
    cursor: not-allowed;
  }

  /* Sales Table Specific Styles */
  .sale-number {
    background-color: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 3px;
    font-family: "Courier New", monospace;
    font-size: 0.9rem;
    color: #334155;
  }

  .client-info {
    font-weight: 500;
    color: #333;
  }

  .payment-method {
    background: #e0f2fe;
    color: #0369a1;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 600;
    text-transform: capitalize;
  }

  .status-badge {
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.8rem;
    font-weight: 600;
    text-transform: capitalize;
  }

  .status-badge.completada {
    background-color: #d1fae5;
    color: #065f46;
  }

  .status-badge.cancelada {
    background-color: #fee2e2;
    color: #991b1b;
  }

  .date-cell {
    font-family: monospace;
    font-size: 0.9rem;
    color: #666;
  }

  .actions {
    display: flex;
    gap: 0.5rem;
  }

  .btn-cancel-sale,
  .btn-view {
    padding: 0.4rem 0.8rem;
    border-radius: 4px;
    font-size: 0.85rem;
    font-weight: 500;
    text-decoration: none;
    transition: background-color 0.2s;
    border: none;
    cursor: pointer;
  }

  .btn-cancel-sale {
    background: #fee2e2;
    color: #dc2626;
  }

  .btn-cancel-sale:hover {
    background: #fecaca;
  }

  .btn-view {
    background: #e0f2fe;
    color: #0369a1;
    display: inline-block;
  }

  .btn-view:hover {
    background: #bae6fd;
  }

  .empty-state {
    text-align: center;
    color: #666;
    font-style: italic;
    padding: 2rem;
  }

  /* Modal de Email Styles */
  .modal-email {
    background: white;
    border-radius: 10px;
    width: 100%;
    max-width: 600px;
    max-height: 90vh;
    overflow: hidden;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
  }

  .email-info {
    background: #f0fff4;
    border: 1px solid #68d391;
    border-radius: 8px;
    padding: 1rem;
    margin-bottom: 1.5rem;
  }

  .success-message {
    margin: 0 0 0.5rem 0;
    color: #2f855a;
    font-weight: 600;
    font-size: 1rem;
  }

  .email-subtitle {
    margin: 0;
    color: #2f855a;
    font-size: 0.9rem;
  }

  .btn-skip,
  .btn-send-email {
    padding: 0.75rem 1.5rem;
    border-radius: 5px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
    border: none;
  }

  .btn-skip {
    background: #e2e8f0;
    color: #4a5568;
  }

  .btn-skip:hover:not(:disabled) {
    background: #cbd5e0;
  }

  .btn-send-email {
    background: #4299e1;
    color: white;
  }

  .btn-send-email:hover:not(:disabled) {
    background: #3182ce;
  }

  .btn-skip:disabled,
  .btn-send-email:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  /* Responsive */
  @media (max-width: 768px) {
    .page-header {
      flex-direction: column;
      gap: 1rem;
      align-items: stretch;
    }

    .form-grid,
    .totals-grid {
      grid-template-columns: 1fr;
    }

    .modal-sales,
    .modal-email {
      width: 95%;
      margin: 1rem;
    }

    .sales-table {
      font-size: 0.85rem;
    }

    .sales-table th,
    .sales-table td {
      padding: 0.5rem;
    }

    .actions {
      flex-direction: column;
    }

    .client-type-selector {
      flex-direction: column;
      gap: 0.75rem;
    }
  }

  @media (max-width: 480px) {
    .modal-content {
      padding: 1rem;
    }

    .step-section {
      padding: 1rem;
    }

    .modal-header,
    .modal-footer {
      padding: 1rem;
    }

    .modal-email .modal-content {
      padding: 1rem;
    }

    .email-info {
      padding: 0.75rem;
    }

    .btn-skip,
    .btn-send-email {
      padding: 0.6rem 1rem;
      font-size: 0.9rem;
    }
  }
</style>
