<script>
	import { page } from '$app/stores';
	import { invalidate } from '$app/navigation';
	import { onMount, onDestroy } from 'svelte';
	import { notify } from '$lib/stores/notifications.js';
	import { saveItem, deleteItem } from '$lib/utils/dataOperations.js';
	import { setupAutoRefresh, cleanupAutoRefresh } from '$lib/utils/autoRefresh.js';

	export let data;
	$: user = $page.data.user;
	$: categorias = data.categorias || [];

	// Setup auto-refresh
	onMount(() => {
		setupAutoRefresh('categorias');
	});

	onDestroy(() => {
		cleanupAutoRefresh('categorias');
	});

	let showModal = false;
	let editMode = false;
	let loading = false;
	let error = '';

	// Formulario
	let formData = {
		id: '',
		nombre: '',
		descripcion: ''
	};

	function openModal(categoria = null) {
		editMode = !!categoria;
		if (categoria) {
			formData = {
				id: categoria.id,
				nombre: categoria.nombre,
				descripcion: categoria.descripcion || ''
			};
		} else {
			formData = {
				id: '',
				nombre: '',
				descripcion: ''
			};
		}
		showModal = true;
		error = '';
	}

	function closeModal() {
		showModal = false;
		editMode = false;
		error = '';
	}

	async function saveCategoria() {
		if (!formData.nombre) {
			error = 'El nombre es obligatorio';
			return;
		}

		loading = true;
		error = '';

		try {
			const method = editMode ? 'PUT' : 'POST';
			
			await saveItem(
				async () => {
					const response = await fetch('/dashboard/categorias', {
						method,
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify(formData)
					});

					const result = await response.json();

					if (!response.ok) {
						throw new Error(result.error || 'Error al guardar la categoría');
					}
					
					return result;
				},
				editMode,
				'categorias',
				'Categoría'
			);
			
			closeModal();
		} catch (err) {
			error = err.message || 'Error de conexión';
		} finally {
			loading = false;
		}
	}

	async function deleteCategoria(id, nombre) {
		try {
			await deleteItem(
				async () => {
					const response = await fetch('/dashboard/categorias', {
						method: 'DELETE',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({ id })
					});

					const result = await response.json();

					if (!response.ok) {
						throw new Error(result.error || 'Error al eliminar la categoría');
					}
					
					return result;
				},
				nombre,
				'categorias'
			);
		} catch (err) {
			// El error ya se maneja en deleteItem
			console.error('Error al eliminar categoría:', err);
		}
	}

	function formatDate(dateString) {
		return new Date(dateString).toLocaleDateString('es-AR');
	}
</script>

<svelte:head>
	<title>Categorías - Sistema de Stock</title>
</svelte:head>

<div class="page-header">
	<h1>Gestión de Categorías</h1>
	<button class="btn-primary" on:click={() => openModal()}>
		➕ Nueva Categoría
	</button>
</div>

<div class="table-container">
	<table class="categories-table">
		<thead>
			<tr>
				<th>Nombre</th>
				<th>Descripción</th>
				<th>Fecha de Creación</th>
				<th class="actions-column">Acciones</th>
			</tr>
		</thead>
		<tbody>
			{#each categorias as categoria (categoria.id)}
			<tr>
				<td class="category-name">{categoria.nombre}</td>
				<td class="category-desc">
					{categoria.descripcion || 'Sin descripción'}
				</td>
				<td class="category-date">
					{formatDate(categoria.created_at)}
				</td>
				<td class="category-actions">
					<button class="btn-edit" on:click={() => openModal(categoria)}>
						✏️
					</button>
					{#if user.rol === 'administrador'}
					<button class="btn-delete" on:click={() => deleteCategoria(categoria.id, categoria.nombre)}>
						🗑️
					</button>
					{/if}
				</td>
			</tr>
			{/each}
		</tbody>
	</table>
</div>

{#if categorias.length === 0}
<div class="empty-state">
	<h2>📂 No hay categorías</h2>
	<p>Comience creando categorías para organizar mejor sus productos.</p>
</div>
{/if}

{#if showModal}
<div class="modal-overlay" on:click={closeModal}>
	<div class="modal" on:click|stopPropagation>
		<div class="modal-header">
			<h2>{editMode ? 'Editar Categoría' : 'Nueva Categoría'}</h2>
			<button class="modal-close" on:click={closeModal}>×</button>
		</div>

		<form on:submit|preventDefault={saveCategoria}>
			<div class="form-content">
				<div class="form-group">
					<label for="nombre">Nombre *</label>
					<input
						type="text"
						id="nombre"
						bind:value={formData.nombre}
						required
						disabled={loading}
						placeholder="Nombre de la categoría"
					/>
				</div>

				<div class="form-group">
					<label for="descripcion">Descripción</label>
					<textarea
						id="descripcion"
						bind:value={formData.descripcion}
						disabled={loading}
						placeholder="Descripción de la categoría (opcional)"
					></textarea>
				</div>

				{#if error}
				<div class="error">{error}</div>
				{/if}
			</div>

			<div class="modal-footer">
				<button type="button" class="btn-cancel" on:click={closeModal}>
					Cancelar
				</button>
				<button type="submit" class="btn-save" disabled={loading}>
					{loading ? 'Guardando...' : 'Guardar'}
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
		margin-bottom: 2rem;
	}

	.categories-table {
		width: 100%;
		border-collapse: collapse;
	}

	.categories-table thead {
		background: #f8f9fa;
	}

	.categories-table th {
		padding: 1rem;
		text-align: left;
		font-weight: 600;
		color: #333;
		border-bottom: 2px solid #dee2e6;
		font-size: 0.95rem;
	}

	.categories-table td {
		padding: 1rem;
		border-bottom: 1px solid #dee2e6;
		vertical-align: middle;
	}

	.categories-table tr:hover {
		background-color: #f8f9fa;
	}

	.category-name {
		font-weight: 500;
		color: #333;
		font-size: 1rem;
	}

	.category-desc {
		color: #666;
		max-width: 300px;
		word-wrap: break-word;
	}

	.category-date {
		color: #999;
		font-size: 0.9rem;
	}

	.actions-column {
		width: 120px;
		text-align: center;
	}

	.category-actions {
		display: flex;
		gap: 0.5rem;
		justify-content: center;
	}

	.btn-edit, .btn-delete {
		padding: 0.4rem 0.6rem;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 0.9rem;
		transition: background-color 0.2s;
		min-width: 32px;
		height: 32px;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.btn-edit {
		background-color: #e0f2fe;
		color: #0369a1;
	}

	.btn-edit:hover {
		background-color: #bae6fd;
	}

	.btn-delete {
		background-color: #fee2e2;
		color: #dc2626;
	}

	.btn-delete:hover {
		background-color: #fecaca;
	}

	.category-desc {
		color: #666;
		margin: 0 0 1rem 0;
		line-height: 1.5;
	}

	.category-footer {
		border-top: 1px solid #f0f0f0;
		padding-top: 1rem;
		margin-top: 1rem;
	}

	.category-date {
		font-size: 0.9rem;
		color: #999;
	}

	.empty-state {
		background: white;
		padding: 3rem 2rem;
		border-radius: 10px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
		text-align: center;
		margin-top: 2rem;
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
		max-width: 500px;
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
	.form-group textarea:focus {
		outline: none;
		border-color: #667eea;
	}

	.form-group textarea {
		min-height: 100px;
		resize: vertical;
	}

	.error {
		background-color: #fee;
		color: #c33;
		padding: 0.75rem;
		border-radius: 5px;
		margin-bottom: 1rem;
		border-left: 4px solid #c33;
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

	@media (max-width: 768px) {
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.table-container {
			overflow-x: auto;
		}

		.categories-table {
			min-width: 600px;
		}

		.categories-table th,
		.categories-table td {
			padding: 0.75rem 0.5rem;
		}

		.category-desc {
			max-width: 200px;
		}

		.category-actions {
			gap: 0.25rem;
		}

		.btn-edit, .btn-delete {
			min-width: 28px;
			height: 28px;
			font-size: 0.8rem;
		}
	}
</style>