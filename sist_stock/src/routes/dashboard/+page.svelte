<script>
	export let data;
	
	$: resumen = data.resumen;
	$: productosBajoStock = data.productosBajoStock;
	
	function formatCurrency(value) {
		return new Intl.NumberFormat('es-AR', {
			style: 'currency',
			currency: 'ARS'
		}).format(value);
	}
</script>

<svelte:head>
	<title>Dashboard - Sistema de Stock</title>
</svelte:head>

<div class="dashboard-header">
	<h1>Panel de Control</h1>
	<p>Resumen general del sistema de stock</p>
</div>

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
			<h3>Bajo Stock</h3>
			<p class="stat-number">{resumen.productosBajoStock}</p>
		</div>
	</div>

	<div class="stat-card">
		<div class="stat-icon">💰</div>
		<div class="stat-content">
			<h3>Valor Inventario</h3>
			<p class="stat-number">{formatCurrency(resumen.valorInventario)}</p>
		</div>
	</div>
</div>

{#if productosBajoStock.length > 0}
<div class="alert-section">
	<h2>⚠️ Productos con Stock Bajo</h2>
	<div class="table-container">
		<table class="products-table">
			<thead>
				<tr>
					<th>Código</th>
					<th>Producto</th>
					<th>Categoría</th>
					<th>Stock Actual</th>
					<th>Stock Mínimo</th>
					<th>Diferencia</th>
				</tr>
			</thead>
			<tbody>
				{#each productosBajoStock as producto}
				<tr>
					<td><code>{producto.codigo}</code></td>
					<td>{producto.nombre}</td>
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
				</tr>
				{/each}
			</tbody>
		</table>
	</div>
</div>
{/if}

{#if productosBajoStock.length === 0 && resumen.totalProductos > 0}
<div class="success-message">
	<h2>✅ ¡Todo en orden!</h2>
	<p>No hay productos con stock bajo en este momento.</p>
</div>
{/if}

{#if resumen.totalProductos === 0}
<div class="empty-state">
	<h2>📦 Bienvenido al Sistema de Stock</h2>
	<p>Comience agregando productos y categorías para gestionar su inventario.</p>
</div>
{/if}

<style>
	.dashboard-header {
		margin-bottom: 2rem;
	}

	.dashboard-header h1 {
		color: #333;
		margin: 0 0 0.5rem 0;
		font-size: 2.5rem;
	}

	.dashboard-header p {
		color: #666;
		margin: 0;
		font-size: 1.1rem;
	}

	.stats-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
		gap: 1.5rem;
		margin-bottom: 2rem;
	}

	.stat-card {
		background: white;
		padding: 1.5rem;
		border-radius: 10px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
		display: flex;
		align-items: center;
		gap: 1rem;
		transition: transform 0.2s;
	}

	.stat-card:hover {
		transform: translateY(-2px);
	}

	.stat-card.warning {
		border-left: 4px solid #f59e0b;
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

	.alert-section {
		background: white;
		padding: 1.5rem;
		border-radius: 10px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
		border-left: 4px solid #f59e0b;
		margin-bottom: 2rem;
	}

	.alert-section h2 {
		color: #f59e0b;
		margin: 0 0 1rem 0;
		font-size: 1.5rem;
	}

	.table-container {
		overflow-x: auto;
	}

	.products-table {
		width: 100%;
		border-collapse: collapse;
		background: white;
	}

	.products-table th {
		background-color: #f8f9fa;
		padding: 0.75rem;
		text-align: left;
		font-weight: 600;
		color: #333;
		border-bottom: 2px solid #dee2e6;
	}

	.products-table td {
		padding: 0.75rem;
		border-bottom: 1px solid #dee2e6;
	}

	.products-table tr:hover {
		background-color: #f8f9fa;
	}

	.text-center {
		text-align: center;
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

	.success-message {
		background: white;
		padding: 2rem;
		border-radius: 10px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
		border-left: 4px solid #10b981;
		text-align: center;
	}

	.success-message h2 {
		color: #10b981;
		margin: 0 0 0.5rem 0;
		font-size: 1.5rem;
	}

	.success-message p {
		color: #666;
		margin: 0;
	}

	.empty-state {
		background: white;
		padding: 3rem 2rem;
		border-radius: 10px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
		text-align: center;
	}

	.empty-state h2 {
		color: #333;
		margin: 0 0 0.5rem 0;
		font-size: 1.8rem;
	}

	.empty-state p {
		color: #666;
		margin: 0;
		font-size: 1.1rem;
	}

	code {
		background-color: #f1f5f9;
		padding: 0.25rem 0.5rem;
		border-radius: 3px;
		font-family: 'Courier New', monospace;
		font-size: 0.9rem;
		color: #334155;
	}

	@media (max-width: 768px) {
		.stats-grid {
			grid-template-columns: 1fr;
		}

		.dashboard-header h1 {
			font-size: 2rem;
		}

		.products-table {
			font-size: 0.9rem;
		}

		.stat-card {
			padding: 1rem;
		}
	}
</style>