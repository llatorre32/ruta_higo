<script>
	import { invalidate } from '$app/navigation';
	import { onMount, onDestroy } from 'svelte';
	import { notify } from '$lib/stores/notifications.js';
	import { saveItem } from '$lib/utils/dataOperations.js';
	import { setupAutoRefresh, cleanupAutoRefresh } from '$lib/utils/autoRefresh.js';

	export let data;
	$: movimientos = data.movimientos || [];
	$: productos = data.productos || [];

	// Setup auto-refresh
	onMount(() => {
		setupAutoRefresh('movimientos');
	});

	onDestroy(() => {
		cleanupAutoRefresh('movimientos');
	});

	let showModal = false;
	let loading = false;
	let error = '';
	let success = '';

	// Formulario
	let formData = {
		productoId: '',
		tipo: 'entrada',
		cantidad: '',
		motivo: ''
	};

	function openModal() {
		formData = {
			productoId: '',
			tipo: 'entrada',
			cantidad: '',
			motivo: ''
		};
		showModal = true;
		error = '';
		success = '';
	}

	function closeModal() {
		showModal = false;
		error = '';
		success = '';
	}

	async function saveMovimiento() {
		if (!formData.productoId || !formData.cantidad) {
			error = 'Complete todos los campos obligatorios';
			return;
		}

		loading = true;
		error = '';
		success = '';

		try {
			await saveItem(
				async () => {
					const response = await fetch('/dashboard/movimientos', {
						method: 'POST',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify(formData)
					});

					const result = await response.json();

					if (!response.ok) {
						throw new Error(result.error || 'Error al registrar el movimiento');
					}
					
					return result;
				},
				false, // No es edición
				'movimientos',
				'Movimiento'
			);
			
			// Resetear formulario
			formData = {
				productoId: '',
				tipo: 'entrada',
				cantidad: '',
				motivo: ''
			};
		} catch (err) {
			error = err.message || 'Error de conexión';
		} finally {
			loading = false;
		}
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

	function getProductoActual() {
		if (!formData.productoId) return null;
		return productos.find(p => p.id === formData.productoId);
	}

	$: productoSeleccionado = getProductoActual();
</script>

<svelte:head>
	<title>Movimientos de Stock - Sistema de Stock</title>
</svelte:head>

<div class="page-header">
	<h1>Movimientos de Stock</h1>
	<button class="btn-primary" on:click={openModal}>
		📝 Registrar Movimiento
	</button>
</div>

<div class="table-container">
	<table class="movements-table">
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
			{#each movimientos as movimiento (movimiento.id)}
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
				<td>
					{movimiento.motivo || '-'}
				</td>
				<td>
					<span class="user-name">{movimiento.usuario_nombre}</span>
				</td>
			</tr>
			{/each}
		</tbody>
	</table>

	{#if movimientos.length === 0}
	<div class="empty-state">
		<p>No hay movimientos registrados</p>
	</div>
	{/if}
</div>

{#if showModal}
<div class="modal-overlay" on:click={closeModal}>
	<div class="modal" on:click|stopPropagation>
		<div class="modal-header">
			<h2>Registrar Movimiento de Stock</h2>
			<button class="modal-close" on:click={closeModal}>×</button>
		</div>

		<form on:submit|preventDefault={saveMovimiento}>
			<div class="form-content">
				<div class="form-group">
					<label for="producto">Producto *</label>
					<select id="producto" bind:value={formData.productoId} required disabled={loading}>
						<option value="">Seleccionar producto</option>
						{#each productos as producto}
						<option value={producto.id}>{producto.codigo} - {producto.nombre}</option>
						{/each}
					</select>
				</div>

				{#if productoSeleccionado}
				<div class="product-current-stock">
					<strong>Stock actual:</strong> {productoSeleccionado.stock_actual} unidades
					<br>
					<strong>Stock mínimo:</strong> {productoSeleccionado.stock_minimo} unidades
				</div>
				{/if}

				<div class="form-group">
					<label for="tipo">Tipo de Movimiento *</label>
					<select id="tipo" bind:value={formData.tipo} required disabled={loading}>
						<option value="entrada">📥 Entrada (Agregar stock)</option>
						<option value="salida">📤 Salida (Reducir stock)</option>
						<option value="ajuste">⚖️ Ajuste (Establecer stock)</option>
					</select>
				</div>

				<div class="form-group">
					<label for="cantidad">
						{#if formData.tipo === 'ajuste'}
							Nuevo Stock *
						{:else}
							Cantidad *
						{/if}
					</label>
					<input
						type="number"
						id="cantidad"
						bind:value={formData.cantidad}
						required
						disabled={loading}
						placeholder={formData.tipo === 'ajuste' ? 'Stock final' : 'Cantidad a mover'}
						min={formData.tipo === 'ajuste' ? '0' : '1'}
					/>
					{#if formData.tipo === 'ajuste' && productoSeleccionado && formData.cantidad}
					<div class="adjustment-preview">
						Cambio: {productoSeleccionado.stock_actual} → {formData.cantidad}
						(diferencia: {parseInt(formData.cantidad) - productoSeleccionado.stock_actual})
					</div>
					{/if}
				</div>

				<div class="form-group">
					<label for="motivo">Motivo</label>
					<textarea
						id="motivo"
						bind:value={formData.motivo}
						disabled={loading}
						placeholder="Descripción del movimiento (opcional)"
					></textarea>
				</div>

				{#if error}
				<div class="error">{error}</div>
				{/if}

				{#if success}
				<div class="success">{success}</div>
				{/if}
			</div>

			<div class="modal-footer">
				<button type="button" class="btn-cancel" on:click={closeModal}>
					Cancelar
				</button>
				<button type="submit" class="btn-save" disabled={loading}>
					{loading ? 'Registrando...' : 'Registrar Movimiento'}
				</button>
			</div>
		</form>
	</div>
</div>
{/if}

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

	.table-container {
		background: white;
		border-radius: 10px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
		overflow: hidden;
	}

	.movements-table {
		width: 100%;
		border-collapse: collapse;
	}

	.movements-table th {
		background-color: #f8f9fa;
		padding: 1rem;
		text-align: left;
		font-weight: 600;
		color: #333;
		border-bottom: 2px solid #dee2e6;
	}

	.movements-table td {
		padding: 1rem;
		border-bottom: 1px solid #dee2e6;
		vertical-align: middle;
	}

	.movements-table tr:hover {
		background-color: #f8f9fa;
	}

	.date-time {
		font-family: monospace;
		font-size: 0.9rem;
		color: #666;
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

	.product-name {
		font-weight: 500;
		color: #333;
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

	.text-center {
		text-align: center;
	}

	/* Modal styles */
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

	.modal {
		background: white;
		border-radius: 10px;
		width: 100%;
		max-width: 600px;
		max-height: 90vh;
		overflow-y: auto;
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 1.5rem;
		border-bottom: 1px solid #dee2e6;
	}

	.modal-header h2 {
		margin: 0;
		color: #333;
	}

	.modal-close {
		background: none;
		border: none;
		font-size: 2rem;
		cursor: pointer;
		color: #666;
		padding: 0;
		line-height: 1;
	}

	.form-content {
		padding: 1.5rem;
	}

	.form-group {
		margin-bottom: 1.5rem;
	}

	.form-group label {
		display: block;
		margin-bottom: 0.5rem;
		font-weight: 500;
		color: #333;
	}

	.form-group input,
	.form-group select,
	.form-group textarea {
		width: 100%;
		padding: 0.75rem;
		border: 2px solid #ddd;
		border-radius: 5px;
		font-size: 1rem;
		transition: border-color 0.3s;
		box-sizing: border-box;
	}

	.form-group input:focus,
	.form-group select:focus,
	.form-group textarea:focus {
		outline: none;
		border-color: #667eea;
	}

	.form-group textarea {
		min-height: 100px;
		resize: vertical;
	}

	.product-current-stock {
		background-color: #f8f9fa;
		padding: 1rem;
		border-radius: 5px;
		border-left: 4px solid #667eea;
		margin-bottom: 1.5rem;
		font-size: 0.9rem;
		color: #333;
	}

	.adjustment-preview {
		margin-top: 0.5rem;
		padding: 0.5rem;
		background-color: #f0f9ff;
		border-radius: 3px;
		font-size: 0.9rem;
		color: #0369a1;
	}

	.error {
		background-color: #fee;
		color: #c33;
		padding: 0.75rem;
		border-radius: 5px;
		margin-bottom: 1rem;
		border-left: 4px solid #c33;
	}

	.success {
		background-color: #f0f9ff;
		color: #0369a1;
		padding: 0.75rem;
		border-radius: 5px;
		margin-bottom: 1rem;
		border-left: 4px solid #0369a1;
	}

	.modal-footer {
		display: flex;
		justify-content: flex-end;
		gap: 1rem;
		padding: 1.5rem;
		border-top: 1px solid #dee2e6;
	}

	.btn-cancel, .btn-save {
		padding: 0.75rem 1.5rem;
		border-radius: 5px;
		font-size: 1rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.2s;
		border: none;
	}

	.btn-cancel {
		background: #f8f9fa;
		color: #333;
	}

	.btn-cancel:hover {
		background: #e9ecef;
	}

	.btn-save {
		background: #28a745;
		color: white;
	}

	.btn-save:hover:not(:disabled) {
		background: #218838;
	}

	.btn-save:disabled {
		background: #ccc;
		cursor: not-allowed;
	}

	.empty-state {
		padding: 2rem;
		text-align: center;
		color: #666;
	}

	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.movements-table {
			font-size: 0.9rem;
		}

		.movements-table th,
		.movements-table td {
			padding: 0.5rem;
		}
	}
</style>