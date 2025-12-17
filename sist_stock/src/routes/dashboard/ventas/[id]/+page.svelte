<script>
  import { page } from '$app/stores';
  import { notify } from '$lib/stores/notifications.js';
  
  export let data;
  $: venta = data.venta;
  
  function formatCurrency(amount) {
    return new Intl.NumberFormat("es-AR", {
      style: "currency",
      currency: "ARS",
    }).format(amount);
  }
  
  function formatDate(dateString) {
    return new Date(dateString).toLocaleString('es-AR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  async function cancelarVenta() {
    if (!confirm('¿Está seguro de cancelar esta venta? Esta acción no se puede deshacer.')) {
      return;
    }
    
    try {
      const response = await fetch(`/api/ventas/${venta.id}`, {
        method: 'DELETE'
      });
      
      if (response.ok) {
        notify.success('Venta cancelada correctamente');
        // Recargar los datos
        window.location.href = '/dashboard/ventas';
      } else {
        const error = await response.json();
        notify.error('Error: ' + error.error);
      }
    } catch (error) {
      console.error('Error al cancelar venta:', error);
      notify.error('Error al cancelar la venta');
    }
  }
  
  async function imprimirComprobante() {
    window.print();
  }
</script>

<svelte:head>
  <title>Detalle de Venta #{venta.numero_venta} - Sistema de Stock</title>
</svelte:head>

<div class="page-header">
  <div class="header-content">
    <div class="title-section">
      <h1>Detalle de Venta</h1>
      <div class="venta-number">#{venta.numero_venta}</div>
    </div>
    <div class="actions">
      <button class="btn-print" on:click={imprimirComprobante}>
        🖨️ Imprimir
      </button>
      {#if venta.estado === 'completada'}
        <button class="btn-cancel" on:click={cancelarVenta}>
          ❌ Cancelar Venta
        </button>
      {/if}
      <a href="/dashboard/ventas" class="btn-back">
        ← Volver a Ventas
      </a>
    </div>
  </div>
</div>

<div class="detail-container">
  <!-- Información de la Venta -->
  <div class="info-section">
    <h2>Información General</h2>
    <div class="info-grid">
      <div class="info-item">
        <label>Número de Venta:</label>
        <span class="venta-code">#{venta.numero_venta}</span>
      </div>
      <div class="info-item">
        <label>Estado:</label>
        <span class="status-badge {venta.estado}">
          {venta.estado === 'completada' ? 'Completada' : 'Cancelada'}
        </span>
      </div>
      <div class="info-item">
        <label>Fecha y Hora:</label>
        <span>{formatDate(venta.created_at)}</span>
      </div>
      <div class="info-item">
        <label>Método de Pago:</label>
        <span class="payment-method">{venta.metodo_pago}</span>
      </div>
      <div class="info-item">
        <label>Vendedor:</label>
        <span>{venta.usuario_nombre}</span>
      </div>
    </div>
  </div>
  
  <!-- Información del Cliente -->
  <div class="info-section">
    <h2>Información del Cliente</h2>
    <div class="client-grid">
      <div class="info-item">
        <label>Nombre:</label>
        <span>{venta.cliente_nombre}</span>
      </div>
      {#if venta.cliente_documento}
        <div class="info-item">
          <label>Documento:</label>
          <span>{venta.cliente_documento}</span>
        </div>
      {/if}
      {#if venta.cliente_telefono}
        <div class="info-item">
          <label>Teléfono:</label>
          <span>{venta.cliente_telefono}</span>
        </div>
      {/if}
      {#if venta.cliente_email}
        <div class="info-item">
          <label>Email:</label>
          <span>{venta.cliente_email}</span>
        </div>
      {/if}
    </div>
  </div>
  
  <!-- Productos Vendidos -->
  <div class="info-section">
    <h2>Productos Vendidos</h2>
    <div class="products-table-container">
      <table class="products-table">
        <thead>
          <tr>
            <th>Código</th>
            <th>Producto</th>
            <th>Cantidad</th>
            <th>Precio Unitario</th>
            <th>Subtotal</th>
          </tr>
        </thead>
        <tbody>
          {#each venta.detalles as detalle}
            <tr>
              <td>
                <code class="product-code">{detalle.producto_codigo}</code>
              </td>
              <td class="product-name">{detalle.producto_nombre}</td>
              <td class="quantity">{detalle.cantidad}</td>
              <td class="price">{formatCurrency(detalle.precio_unitario)}</td>
              <td class="price">{formatCurrency(detalle.cantidad * detalle.precio_unitario)}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </div>
  
  <!-- Totales -->
  <div class="totals-section">
    <h2>Totales</h2>
    <div class="totals-content">
      <div class="total-row">
        <span>Subtotal:</span>
        <span class="amount">{formatCurrency(venta.subtotal)}</span>
      </div>
      {#if venta.descuento > 0}
        <div class="total-row discount">
          <span>Descuento:</span>
          <span class="amount">-{formatCurrency(venta.descuento)}</span>
        </div>
      {/if}
      {#if venta.impuestos > 0}
        <div class="total-row taxes">
          <span>Impuestos:</span>
          <span class="amount">{formatCurrency(venta.impuestos)}</span>
        </div>
      {/if}
      <div class="total-row final-total">
        <span>Total:</span>
        <span class="amount total-amount">{formatCurrency(venta.total)}</span>
      </div>
    </div>
  </div>
</div>

<style>
  /* Page Layout */
  .page-header {
    margin-bottom: 2rem;
    border-bottom: 2px solid #e2e8f0;
    padding-bottom: 1rem;
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .title-section {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .title-section h1 {
    margin: 0;
    color: #333;
    font-size: 2rem;
  }
  
  .venta-number {
    background: #667eea;
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-weight: 600;
    font-size: 1.1rem;
  }
  
  .actions {
    display: flex;
    gap: 0.75rem;
    flex-wrap: wrap;
  }
  
  /* Button Styles */
  .btn-print,
  .btn-cancel,
  .btn-back {
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    font-weight: 500;
    text-decoration: none;
    border: none;
    cursor: pointer;
    transition: all 0.2s;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .btn-print {
    background: #e0f2fe;
    color: #0369a1;
  }
  
  .btn-print:hover {
    background: #bae6fd;
  }
  
  .btn-cancel {
    background: #fee2e2;
    color: #dc2626;
  }
  
  .btn-cancel:hover {
    background: #fecaca;
  }
  
  .btn-back {
    background: #f1f5f9;
    color: #475569;
  }
  
  .btn-back:hover {
    background: #e2e8f0;
  }
  
  /* Detail Container */
  .detail-container {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }
  
  .info-section {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }
  
  .info-section h2 {
    background: #f8f9fa;
    margin: 0;
    padding: 1.5rem;
    color: #333;
    font-size: 1.25rem;
    font-weight: 600;
    border-bottom: 1px solid #e9ecef;
  }
  
  /* Info Grid */
  .info-grid,
  .client-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
    padding: 1.5rem;
  }
  
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .info-item label {
    color: #666;
    font-weight: 500;
    font-size: 0.9rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .info-item span {
    color: #333;
    font-weight: 500;
    font-size: 1rem;
  }
  
  .venta-code {
    font-family: 'Courier New', monospace;
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    display: inline-block;
  }
  
  .status-badge {
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.9rem;
    font-weight: 600;
    text-transform: capitalize;
    display: inline-block;
    width: fit-content;
  }
  
  .status-badge.completada {
    background-color: #d1fae5;
    color: #065f46;
  }
  
  .status-badge.cancelada {
    background-color: #fee2e2;
    color: #991b1b;
  }
  
  .payment-method {
    background: #e0f2fe;
    color: #0369a1;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.9rem;
    font-weight: 600;
    text-transform: capitalize;
    display: inline-block;
    width: fit-content;
  }
  
  /* Products Table */
  .products-table-container {
    overflow: hidden;
  }
  
  .products-table {
    width: 100%;
    border-collapse: collapse;
  }
  
  .products-table th {
    background: #f8f9fa;
    padding: 1rem;
    text-align: left;
    font-weight: 600;
    color: #333;
    font-size: 0.9rem;
    border-bottom: 2px solid #dee2e6;
  }
  
  .products-table td {
    padding: 1rem;
    border-bottom: 1px solid #f0f0f0;
    vertical-align: middle;
  }
  
  .products-table tr:hover {
    background: #f8f9fa;
  }
  
  .product-code {
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 0.9rem;
    color: #334155;
  }
  
  .product-name {
    font-weight: 500;
    color: #333;
  }
  
  .quantity {
    text-align: center;
    font-weight: 500;
    color: #333;
  }
  
  .price {
    text-align: right;
    font-weight: 500;
    color: #333;
    font-family: 'Courier New', monospace;
  }
  
  /* Totals Section */
  .totals-section {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    align-self: flex-end;
    min-width: 300px;
  }
  
  .totals-section h2 {
    background: #f8f9fa;
    margin: 0;
    padding: 1.5rem;
    color: #333;
    font-size: 1.25rem;
    font-weight: 600;
    border-bottom: 1px solid #e9ecef;
  }
  
  .totals-content {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .total-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 1rem;
  }
  
  .total-row span:first-child {
    color: #666;
    font-weight: 500;
  }
  
  .amount {
    font-weight: 600;
    color: #333;
    font-family: 'Courier New', monospace;
  }
  
  .total-row.discount .amount {
    color: #dc2626;
  }
  
  .total-row.taxes .amount {
    color: #059669;
  }
  
  .final-total {
    border-top: 2px solid #e9ecef;
    padding-top: 1rem;
    font-size: 1.2rem;
  }
  
  .final-total span:first-child {
    color: #333;
    font-weight: 600;
  }
  
  .total-amount {
    color: #667eea;
    font-size: 1.3rem;
  }
  
  /* Print Styles */
  @media print {
    .page-header .actions {
      display: none;
    }
    
    .info-section,
    .totals-section {
      box-shadow: none;
      border: 1px solid #ddd;
    }
    
    body {
      font-size: 12px;
    }
  }
  
  /* Responsive */
  @media (max-width: 768px) {
    .header-content {
      flex-direction: column;
      align-items: stretch;
    }
    
    .actions {
      justify-content: center;
    }
    
    .info-grid,
    .client-grid {
      grid-template-columns: 1fr;
    }
    
    .totals-section {
      align-self: stretch;
    }
    
    .products-table-container {
      overflow-x: auto;
    }
    
    .products-table {
      min-width: 500px;
    }
  }
  
  @media (max-width: 480px) {
    .title-section {
      flex-direction: column;
      gap: 0.5rem;
    }
    
    .title-section h1 {
      font-size: 1.5rem;
    }
    
    .venta-number {
      font-size: 1rem;
      padding: 0.4rem 0.8rem;
    }
    
    .actions {
      flex-direction: column;
    }
    
    .btn-print,
    .btn-cancel,
    .btn-back {
      padding: 0.6rem 1rem;
      font-size: 0.9rem;
    }
    
    .info-section h2,
    .totals-section h2 {
      padding: 1rem;
      font-size: 1.1rem;
    }
    
    .info-grid,
    .client-grid,
    .totals-content {
      padding: 1rem;
    }
    
    .products-table th,
    .products-table td {
      padding: 0.75rem 0.5rem;
    }
  }
</style>