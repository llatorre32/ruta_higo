<script>
	export let data;
	$: productosBajoStock = data.productosBajoStock || [];
	$: resumen = data.resumen || {
		totalProductos: 0,
		productosBajoStock: 0,
		valorInventario: 0,
		totalCategorias: 0
	};
	$: ultimosMovimientos = data.ultimosMovimientos || [];
	$: ventasMensuales = data.ventasMensuales || [];
	$: ventasPorVendedor = data.ventasPorVendedor || [];
	$: productosMasVendidos = data.productosMasVendidos || [];

	function formatCurrency(value) {
		return new Intl.NumberFormat('es-AR', {
			style: 'currency',
			currency: 'ARS'
		}).format(value);
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

	function getTipoClass(tipo) {
		switch (tipo) {
			case 'entrada': return 'tipo-entrada';
			case 'salida': return 'tipo-salida';
			case 'ajuste': return 'tipo-ajuste';
			default: return '';
		}
	}

	function getTipoText(tipo) {
		switch (tipo) {
			case 'entrada': return '📥 Entrada';
			case 'salida': return '📤 Salida';
			case 'ajuste': return '⚖️ Ajuste';
			default: return tipo;
		}
	}

	function printReport() {
		window.print();
	}
</script>

<svelte:head>
	<title>Reportes - Sistema de Stock</title>
</svelte:head>

<div class="page-header">
	<h1>Reportes del Sistema</h1>
	<button class="btn-print" on:click={printReport}>
		🖨️ Imprimir
	</button>
</div>

<!-- Resumen general -->
<section class="report-section">
	<h2>Resumen General</h2>
	<div class="stats-grid">
		<div class="stat-card">
			<div class="stat-icon">📦</div>
			<div class="stat-content">
				<h3>Total Productos</h3>
				<p class="stat-number">{resumen.totalProductos}</p>
			</div>
		</div>

		<div class="stat-card">
			<div class="stat-icon">🏷️</div>
			<div class="stat-content">
				<h3>Categorías</h3>
				<p class="stat-number">{resumen.totalCategorias}</p>
			</div>
		</div>

		<div class="stat-card {resumen.productosBajoStock > 0 ? 'warning' : ''}">
			<div class="stat-icon">⚠️</div>
			<div class="stat-content">
				<h3>Productos Bajo Stock</h3>
				<p class="stat-number">{resumen.productosBajoStock}</p>
			</div>
		</div>

		<div class="stat-card">
			<div class="stat-icon">💰</div>
			<div class="stat-content">
				<h3>Valor Total Inventario</h3>
				<p class="stat-number">{formatCurrency(resumen.valorInventario)}</p>
			</div>
		</div>
	</div>
</section>

<!-- Productos con bajo stock -->
{#if productosBajoStock.length > 0}
<section class="report-section">
	<h2>⚠️ Productos con Stock Bajo</h2>
	<div class="table-container">
		<table class="report-table">
			<thead>
				<tr>
					<th>Código</th>
					<th>Producto</th>
					<th>Categoría</th>
					<th>Stock Actual</th>
					<th>Stock Mínimo</th>
					<th>Diferencia</th>
					<th>Valor en Stock</th>
				</tr>
			</thead>
			<tbody>
				{#each productosBajoStock as producto}
				<tr>
					<td><code>{producto.codigo}</code></td>
					<td>
						<div class="product-name">{producto.nombre}</div>
						{#if producto.descripcion}
						<div class="product-desc">{producto.descripcion}</div>
						{/if}
					</td>
					<td>{producto.categoria_nombre || 'Sin categoría'}</td>
					<td class="text-center">
						<span class="stock-badge {producto.stock_actual === 0 ? 'danger' : 'warning'}">
							{producto.stock_actual}
						</span>
					</td>
					<td class="text-center">{producto.stock_minimo}</td>
					<td class="text-center">
						<span class="stock-difference">
							{producto.stock_actual - producto.stock_minimo}
						</span>
					</td>
					<td class="text-right">
						{formatCurrency(producto.stock_actual * producto.precio_compra)}
					</td>
				</tr>
				{/each}
			</tbody>
		</table>
	</div>
</section>
{/if}

<!-- Últimos movimientos -->
<section class="report-section">
	<h2>Últimos 20 Movimientos</h2>
	<div class="table-container">
		<table class="report-table">
			<thead>
				<tr>
					<th>Fecha</th>
					<th>Producto</th>
					<th>Tipo</th>
					<th>Cantidad</th>
					<th>Motivo</th>
					<th>Usuario</th>
				</tr>
			</thead>
			<tbody>
				{#each ultimosMovimientos as movimiento}
				<tr>
					<td>
						<span class="date-time">{formatDate(movimiento.created_at)}</span>
					</td>
					<td>
						<div class="product-info">
							<code class="product-code">{movimiento.producto_codigo}</code>
							<div class="product-name">{movimiento.producto_nombre}</div>
						</div>
					</td>
					<td>
						<span class="tipo-badge {getTipoClass(movimiento.tipo)}">
							{getTipoText(movimiento.tipo)}
						</span>
					</td>
					<td class="text-center">
						<span class="cantidad {movimiento.tipo}">
							{#if movimiento.tipo === 'entrada'}+{/if}
							{#if movimiento.tipo === 'salida'}-{/if}
							{movimiento.cantidad}
						</span>
					</td>
					<td>{movimiento.motivo || '-'}</td>
					<td>
						<span class="user-name">{movimiento.usuario_nombre}</span>
					</td>
				</tr>
				{/each}
			</tbody>
		</table>

		{#if ultimosMovimientos.length === 0}
		<div class="empty-state">
			<p>No hay movimientos registrados</p>
		</div>
		{/if}
	</div>
</section>

<!-- Reportes de Ventas -->
<section class="report-section">
	<h2>📊 Reportes de Ventas Mensuales</h2>
	<div class="table-container">
		<table class="report-table">
			<thead>
				<tr>
					<th>Mes</th>
					<th>Cantidad Ventas</th>
					<th>Total Ventas</th>
					<th>Promedio por Venta</th>
					<th>Subtotal</th>
					<th>Descuentos</th>
					<th>Impuestos</th>
				</tr>
			</thead>
			<tbody>
				{#each ventasMensuales as mes}
				<tr>
					<td>
						<div class="month-info">
							<div class="month-name">{mes.mes_nombre}</div>
							<div class="month-code">{mes.año}</div>
						</div>
					</td>
					<td class="text-center">
						<span class="sales-count">{mes.cantidad_ventas}</span>
					</td>
					<td class="text-right">
						<span class="total-amount">{formatCurrency(mes.total_ventas || 0)}</span>
					</td>
					<td class="text-right">
						<span class="average-amount">{formatCurrency(mes.promedio_venta || 0)}</span>
					</td>
					<td class="text-right">
						{formatCurrency(mes.total_subtotal || 0)}
					</td>
					<td class="text-right">
						<span class="discount-amount">{formatCurrency(mes.total_descuentos || 0)}</span>
					</td>
					<td class="text-right">
						{formatCurrency(mes.total_impuestos || 0)}
					</td>
				</tr>
				{/each}
			</tbody>
		</table>

		{#if ventasMensuales.length === 0}
		<div class="empty-state">
			<p>No hay datos de ventas mensuales</p>
		</div>
		{/if}
	</div>
</section>

<section class="report-section">
	<h2>👥 Reporte de Ventas por Vendedor</h2>
	<div class="table-container">
		<table class="report-table">
			<thead>
				<tr>
					<th>Vendedor</th>
					<th>Cantidad Ventas</th>
					<th>Total Ventas</th>
					<th>Promedio</th>
					<th>Venta Mín</th>
					<th>Venta Máx</th>
					<th>% del Total</th>
				</tr>
			</thead>
			<tbody>
				{#each ventasPorVendedor as vendedor}
				<tr>
					<td>
						<div class="seller-info">
							<div class="seller-name">{vendedor.vendedor_nombre}</div>
							<div class="seller-email">{vendedor.vendedor_email}</div>
						</div>
					</td>
					<td class="text-center">
						<span class="sales-count">{vendedor.cantidad_ventas}</span>
					</td>
					<td class="text-right">
						<span class="total-amount">{formatCurrency(vendedor.total_ventas || 0)}</span>
					</td>
					<td class="text-right">
						{formatCurrency(vendedor.promedio_venta || 0)}
					</td>
					<td class="text-right">
						{formatCurrency(vendedor.venta_minima || 0)}
					</td>
					<td class="text-right">
						{formatCurrency(vendedor.venta_maxima || 0)}
					</td>
					<td class="text-center">
						<span class="percentage">{vendedor.porcentaje_total || 0}%</span>
					</td>
				</tr>
				{/each}
			</tbody>
		</table>

		{#if ventasPorVendedor.length === 0}
		<div class="empty-state">
			<p>No hay datos de ventas por vendedor</p>
		</div>
		{/if}
	</div>
</section>

<section class="report-section">
	<h2>🏆 Productos Más Vendidos</h2>
	<div class="table-container">
		<table class="report-table">
			<thead>
				<tr>
					<th>Producto</th>
					<th>Categoría</th>
					<th>Cantidad Vendida</th>
					<th>Ventas Realizadas</th>
					<th>Total Ingresos</th>
					<th>Precio Promedio</th>
					<th>% de Ventas</th>
					<th>Stock Actual</th>
				</tr>
			</thead>
			<tbody>
				{#each productosMasVendidos as producto}
				<tr>
					<td>
						<div class="product-info">
							<div class="product-name">{producto.producto_nombre}</div>
							<div class="product-code">{producto.producto_codigo}</div>
						</div>
					</td>
					<td>{producto.categoria_nombre || 'Sin categoría'}</td>
					<td class="text-center">
						<span class="quantity-sold">{producto.total_vendido}</span>
					</td>
					<td class="text-center">
						<span class="sales-count">{producto.ventas_realizadas}</span>
					</td>
					<td class="text-right">
						<span class="total-revenue">{formatCurrency(producto.total_ingresos || 0)}</span>
					</td>
					<td class="text-right">
						{formatCurrency(producto.precio_promedio || 0)}
					</td>
					<td class="text-center">
						<span class="percentage">{producto.porcentaje_ventas || 0}%</span>
					</td>
					<td class="text-center">
						<span class="stock-amount {producto.stock_actual <= 5 ? 'stock-low' : producto.stock_actual <= 10 ? 'stock-medium' : 'stock-high'}">
							{producto.stock_actual}
						</span>
					</td>
				</tr>
				{/each}
			</tbody>
		</table>

		{#if productosMasVendidos.length === 0}
		<div class="empty-state">
			<p>No hay datos de productos vendidos</p>
		</div>
		{/if}
	</div>
</section>

<style>
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

	.btn-print {
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

	.btn-print:hover {
		background: #5a67d8;
	}

	.report-section {
		background: white;
		border-radius: 10px;
		padding: 1.5rem;
		margin-bottom: 2rem;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
	}

	.report-section h2 {
		margin: 0 0 1.5rem 0;
		color: #333;
		font-size: 1.5rem;
		border-bottom: 2px solid #f0f0f0;
		padding-bottom: 0.5rem;
	}

	.stats-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
		gap: 1.5rem;
	}

	.stat-card {
		background: #f8f9fa;
		padding: 1.5rem;
		border-radius: 10px;
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.stat-card.warning {
		border-left: 4px solid #f59e0b;
		background: #fefbf3;
	}

	.stat-icon {
		font-size: 2.5rem;
		opacity: 0.8;
	}

	.stat-content h3 {
		margin: 0;
		color: #666;
		font-size: 0.9rem;
		font-weight: 500;
		text-transform: uppercase;
		letter-spacing: 0.5px;
	}

	.stat-number {
		margin: 0.25rem 0 0 0;
		font-size: 2rem;
		font-weight: 700;
		color: #333;
	}

	.table-container {
		overflow-x: auto;
	}

	.report-table {
		width: 100%;
		border-collapse: collapse;
		margin-top: 1rem;
	}

	.report-table th {
		background-color: #f8f9fa;
		padding: 0.75rem;
		text-align: left;
		font-weight: 600;
		color: #333;
		border-bottom: 2px solid #dee2e6;
	}

	.report-table td {
		padding: 0.75rem;
		border-bottom: 1px solid #dee2e6;
		vertical-align: middle;
	}

	.report-table tr:hover {
		background-color: #f8f9fa;
	}

	.product-name {
		font-weight: 500;
		color: #333;
	}

	.product-desc {
		font-size: 0.9rem;
		color: #666;
		margin-top: 0.25rem;
	}

	.product-info {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.product-code {
		background-color: #f1f5f9;
		padding: 0.25rem 0.5rem;
		border-radius: 3px;
		font-family: 'Courier New', monospace;
		font-size: 0.8rem;
		color: #334155;
		align-self: flex-start;
	}

	.stock-badge {
		padding: 0.25rem 0.75rem;
		border-radius: 20px;
		font-weight: 600;
		font-size: 0.9rem;
	}

	.stock-badge.warning {
		background-color: #fef3c7;
		color: #92400e;
	}

	.stock-badge.danger {
		background-color: #fee2e2;
		color: #991b1b;
	}

	.stock-difference {
		font-weight: 600;
		color: #dc2626;
	}

	.date-time {
		font-family: monospace;
		font-size: 0.9rem;
		color: #666;
	}

	.tipo-badge {
		padding: 0.25rem 0.75rem;
		border-radius: 20px;
		font-size: 0.8rem;
		font-weight: 600;
	}

	.tipo-entrada {
		background-color: #d1fae5;
		color: #065f46;
	}

	.tipo-salida {
		background-color: #fee2e2;
		color: #991b1b;
	}

	.tipo-ajuste {
		background-color: #ddd6fe;
		color: #5b21b6;
	}

	.cantidad {
		font-family: monospace;
		font-weight: 600;
		font-size: 1.1rem;
	}

	.cantidad.entrada {
		color: #059669;
	}

	.cantidad.salida {
		color: #dc2626;
	}

	.cantidad.ajuste {
		color: #7c3aed;
	}

	.user-name {
		color: #666;
		font-size: 0.9rem;
	}

	.text-center { text-align: center; }
	.text-right { text-align: right; }

	.empty-state {
		padding: 2rem;
		text-align: center;
		color: #666;
	}

	code {
		background-color: #f1f5f9;
		padding: 0.25rem 0.5rem;
		border-radius: 3px;
		font-family: 'Courier New', monospace;
		font-size: 0.9rem;
		color: #334155;
	}

	@media print {
		.page-header {
			margin-bottom: 1rem;
		}

		.btn-print {
			display: none;
		}

		.report-section {
			box-shadow: none;
			margin-bottom: 1rem;
			break-inside: avoid;
		}

		.stats-grid {
			grid-template-columns: repeat(2, 1fr);
			gap: 1rem;
		}

		.stat-card {
			padding: 1rem;
		}

		.report-table {
			font-size: 0.9rem;
		}

		.report-table th,
		.report-table td {
			padding: 0.5rem;
		}
	}

	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.stats-grid {
			grid-template-columns: 1fr;
		}

		.stat-card {
			padding: 1rem;
		}

		.report-table {
			font-size: 0.9rem;
		}

		.report-table th,
		.report-table td {
			padding: 0.5rem;
		}
	}

	/* Estilos específicos para reportes de ventas */
	.month-info, .seller-info {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.month-name, .seller-name {
		font-weight: 500;
		color: #333;
	}

	.month-code, .seller-email {
		font-size: 0.9rem;
		color: #666;
		font-family: monospace;
	}

	.sales-count {
		background-color: #e0f2fe;
		color: #0277bd;
		padding: 0.25rem 0.75rem;
		border-radius: 20px;
		font-size: 0.9rem;
		font-weight: 600;
	}

	.total-amount {
		font-weight: 600;
		color: #2e7d32;
		font-size: 1.1rem;
	}

	.average-amount, .total-revenue {
		font-weight: 500;
		color: #1976d2;
	}

	.discount-amount {
		color: #f57c00;
		font-weight: 500;
	}

	.percentage {
		background-color: #f3e5f5;
		color: #7b1fa2;
		padding: 0.25rem 0.75rem;
		border-radius: 20px;
		font-size: 0.9rem;
		font-weight: 600;
	}

	.quantity-sold {
		background-color: #e8f5e8;
		color: #2e7d32;
		padding: 0.25rem 0.75rem;
		border-radius: 20px;
		font-size: 0.9rem;
		font-weight: 600;
	}

	.stock-amount {
		padding: 0.25rem 0.75rem;
		border-radius: 20px;
		font-size: 0.9rem;
		font-weight: 600;
	}

	.stock-high {
		background-color: #e8f5e8;
		color: #2e7d32;
	}

	.stock-medium {
		background-color: #fff3e0;
		color: #f57c00;
	}

	.stock-low {
		background-color: #ffebee;
		color: #d32f2f;
	}

	.text-center {
		text-align: center;
	}

	.text-right {
		text-align: right;
	}
</style>