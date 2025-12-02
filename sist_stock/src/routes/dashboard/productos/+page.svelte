<script>
	import { page } from '$app/stores';
	import { invalidate } from '$app/navigation';
	import { onMount, onDestroy } from 'svelte';
	import { notify } from '$lib/stores/notifications.js';
	import { saveItem, deleteItem } from '$lib/utils/dataOperations.js';
	import { setupAutoRefresh, cleanupAutoRefresh } from '$lib/utils/autoRefresh.js';

	export let data;
	$: user = $page.data.user;
	$: productos = data.productos;
	$: categorias = data.categorias;

	// Setup auto-refresh
	onMount(() => {
		setupAutoRefresh('productos');
	});

	onDestroy(() => {
		cleanupAutoRefresh('productos');
	});

	let showModal = false;
	let editMode = false;
	let loading = false;
	let error = '';
	let searchTerm = '';

	// Formulario
	let formData = {
		id: '',
		codigo: '',
		nombre: '',
		descripcion: '',
		categoriaId: '',
		precioCompra: 0,
		precioVenta: 0,
		stockMinimo: 0,
		stockMaximo: 0,
		stockInicial: 0,
		stockAjuste: 0,
		tipoAjuste: 'suma', // 'suma' o 'reemplazo'
		imagen: null
	};

	let fileInput;
	let previewImage = null;

	// Filtros
	$: filteredProductos = productos.filter(producto => {
		if (!searchTerm) return true;
		const search = searchTerm.toLowerCase();
		return producto.codigo.toLowerCase().includes(search) ||
		       producto.nombre.toLowerCase().includes(search) ||
		       (producto.categoria_nombre && producto.categoria_nombre.toLowerCase().includes(search));
	});

	function openModal(producto = null) {
		editMode = !!producto;
		previewImage = null;
		if (producto) {
			formData = {
				id: producto.id,
				codigo: producto.codigo,
				nombre: producto.nombre,
				descripcion: producto.descripcion || '',
				categoriaId: producto.categoria_id || '',
				precioCompra: producto.precio_compra || 0,
				precioVenta: producto.precio_venta || 0,
				stockMinimo: producto.stock_minimo || 0,
				stockMaximo: producto.stock_maximo || 0,
				stockInicial: 0,
				stockAjuste: 0,
				tipoAjuste: 'suma',
				imagen: null
			};
			// Mostrar imagen existente
			if (producto.imagen) {
				previewImage = producto.imagen;
			}
		} else {
			formData = {
				id: '',
				codigo: '',
				nombre: '',
				descripcion: '',
				categoriaId: '',
				precioCompra: 0,
				precioVenta: 0,
				stockMinimo: 0,
				stockMaximo: 0,
				stockInicial: 0,
				stockAjuste: 0,
				tipoAjuste: 'suma',
				imagen: null
			};
		}
		showModal = true;
		error = '';
	}

	function handleImageChange(event) {
		const file = event.target.files[0];
		if (file) {
			// Validar tipo de archivo
			const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
			if (!allowedTypes.includes(file.type)) {
				error = 'Tipo de archivo no válido. Solo se permiten JPEG, PNG, GIF y WebP';
				event.target.value = '';
				return;
			}

			// Validar tamaño (máximo 5MB)
			if (file.size > 5 * 1024 * 1024) {
				error = 'El archivo es demasiado grande. Máximo 5MB';
				event.target.value = '';
				return;
			}

			formData.imagen = file;
			
			// Crear preview
			const reader = new FileReader();
			reader.onload = (e) => {
				previewImage = e.target.result;
			};
			reader.readAsDataURL(file);
			error = '';
		}
	}

	function removeImage() {
		formData.imagen = null;
		previewImage = null;
		if (fileInput) {
			fileInput.value = '';
		}
	}

	function closeModal() {
		showModal = false;
		editMode = false;
		error = '';
	}

	async function saveProducto() {
		if (!formData.codigo || !formData.nombre || !formData.categoriaId) {
			error = 'Complete todos los campos obligatorios (código, nombre y categoría)';
			return;
		}

		// Validación adicional para ajustes de stock en modo edición
		if (editMode && formData.tipoAjuste === 'suma') {
			const productoActual = productos.find(p => p.id === formData.id);
			const stockActual = productoActual?.stock_actual || 0;
			const nuevoStock = stockActual + Number(formData.stockAjuste);
			
			if (nuevoStock < 0) {
				error = 'El ajuste resultaría en stock negativo. Verifique la cantidad.';
				return;
			}
		}

		if (editMode && formData.tipoAjuste === 'reemplazo' && formData.stockAjuste < 0) {
			error = 'El stock no puede ser negativo.';
			return;
		}

		loading = true;
		error = '';

		try {
			const method = editMode ? 'PUT' : 'POST';
			let requestBody;
			let headers = {};

			// Si hay imagen, usar FormData, sino JSON
			if (formData.imagen instanceof File) {
				requestBody = new FormData();
				Object.keys(formData).forEach(key => {
					if (key === 'imagen') {
						requestBody.append(key, formData[key]);
					} else {
						requestBody.append(key, formData[key]);
					}
				});
			} else {
				headers['Content-Type'] = 'application/json';
				const { imagen, ...dataToSend } = formData;
				requestBody = JSON.stringify(dataToSend);
			}
			
			await saveItem(
				async () => {
					const response = await fetch('/dashboard/productos', {
						method,
						headers,
						body: requestBody
					});

					const result = await response.json();
					
					if (!response.ok) {
						throw new Error(result.error || 'Error al guardar el producto');
					}
					
					return result;
				},
				editMode,
				'productos',
				'Producto'
			);
			
			closeModal();
		} catch (err) {
			error = err.message || 'Error de conexión';
		} finally {
			loading = false;
		}
	}

	async function deleteProducto(id, nombre) {
		try {
			await deleteItem(
				async () => {
					const response = await fetch('/dashboard/productos', {
						method: 'DELETE',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({ id })
					});

					const result = await response.json();

					if (!response.ok) {
						throw new Error(result.error || 'Error al eliminar el producto');
					}
					
					return result;
				},
				nombre,
				'productos'
			);
		} catch (err) {
			// El error ya se maneja en deleteItem
			console.error('Error al eliminar producto:', err);
		}
	}

	function getStockStatus(producto) {
		if (producto.stock_actual === 0) return 'sin-stock';
		if (producto.stock_actual <= producto.stock_minimo) return 'bajo-stock';
		return 'ok';
	}
</script>

<svelte:head>
	<title>Productos - Sistema de Stock</title>
</svelte:head>

<div class="page-header">
	<h1>Gestión de Productos</h1>
	<button class="btn-primary" on:click={() => openModal()}>
		➕ Nuevo Producto
	</button>
</div>

<div class="search-bar">
	<input
		type="text"
		placeholder="Buscar por código, nombre o categoría..."
		bind:value={searchTerm}
	/>
</div>

<div class="table-container">
	<table class="products-table">
		<thead>
			<tr>
				<th>Imagen</th>
				<th>Código</th>
				<th>Producto</th>
				<th>Categoría</th>
				<th>Stock Actual</th>
				<th>Stock Mínimo</th>
				<th>Estado</th>
				<th>Acciones</th>
			</tr>
		</thead>
		<tbody>
			{#each filteredProductos as producto (producto.id)}
			<tr>
				<td class="text-center">
					{#if producto.imagen}
						<img src={producto.imagen} alt={producto.nombre} class="product-image" />
					{:else}
						<div class="no-image">Sin imagen</div>
					{/if}
				</td>
				<td><code>{producto.codigo}</code></td>
				<td>
					<div class="product-name">{producto.nombre}</div>
					{#if producto.descripcion}
					<div class="product-desc">{producto.descripcion}</div>
					{/if}
				</td>
				<td>{producto.categoria_nombre || 'Sin categoría'}</td>
				<td class="text-center">
					<span class="stock-number">{producto.stock_actual}</span>
				</td>
				<td class="text-center">
					<span class="stock-number">{producto.stock_minimo}</span>
				</td>
				<td class="text-center">
					<span class="stock-badge {getStockStatus(producto)}">
						{#if getStockStatus(producto) === 'sin-stock'}
							Sin Stock
						{:else if getStockStatus(producto) === 'bajo-stock'}
							Bajo Stock
						{:else}
							OK
						{/if}
					</span>
				</td>
				<td>
					<div class="actions">
						<button class="btn-edit" on:click={() => openModal(producto)}>
							✏️
						</button>
						{#if user.rol === 'administrador'}
						<button class="btn-delete" on:click={() => deleteProducto(producto.id, producto.nombre)}>
							🗑️
						</button>
						{/if}
					</div>
				</td>
			</tr>
			{/each}
		</tbody>
	</table>

	{#if filteredProductos.length === 0}
	<div class="empty-state">
		<p>No se encontraron productos</p>
	</div>
	{/if}
</div>

{#if showModal}
<div class="modal-overlay" on:click={closeModal}>
	<div class="modal" on:click|stopPropagation>
		<div class="modal-header">
			<h2>{editMode ? 'Editar Producto' : 'Nuevo Producto'}</h2>
			<button class="modal-close" on:click={closeModal}>×</button>
		</div>

		<form on:submit|preventDefault={saveProducto}>
			<div class="form-grid">
				<div class="form-group">
					<label for="codigo">Código *</label>
					<input
						type="text"
						id="codigo"
						bind:value={formData.codigo}
						required
						disabled={loading}
						placeholder="SKU001"
					/>
				</div>

				<div class="form-group">
					<label for="nombre">Nombre *</label>
					<input
						type="text"
						id="nombre"
						bind:value={formData.nombre}
						required
						disabled={loading}
						placeholder="Nombre del producto"
					/>
				</div>

				<div class="form-group full-width">
					<label for="descripcion">Descripción</label>
					<textarea
						id="descripcion"
						bind:value={formData.descripcion}
						disabled={loading}
						placeholder="Descripción del producto"
					></textarea>
				</div>

				<div class="form-group full-width">
					<label for="imagen">Imagen del Producto</label>
					<input
						type="file"
						id="imagen"
						accept="image/jpeg,image/png,image/gif,image/webp"
						on:change={handleImageChange}
						disabled={loading}
						bind:this={fileInput}
					/>
					<small class="file-help">Formatos: JPEG, PNG, GIF, WebP. Máximo 5MB</small>
					
					{#if previewImage}
						<div class="image-preview">
							<img src={previewImage} alt="Preview" />
							<button type="button" class="btn-remove-image" on:click={removeImage}>
								Eliminar imagen
							</button>
						</div>
					{/if}
				</div>

				<div class="form-group">
					<label for="categoria">Categoría *</label>
					<select id="categoria" bind:value={formData.categoriaId} disabled={loading} required>
						<option value="">Seleccione una categoría</option>
						{#each categorias as categoria}
						<option value={categoria.id}>{categoria.nombre}</option>
						{/each}
					</select>
				</div>

				<div class="form-group">
					<label for="precioCompra">Precio Compra</label>
					<input
						type="number"
						step="0.01"
						min="0"
						id="precioCompra"
						bind:value={formData.precioCompra}
						disabled={loading}
						placeholder="0.00"
					/>
				</div>

				<div class="form-group">
					<label for="precioVenta">Precio Venta</label>
					<input
						type="number"
						step="0.01"
						min="0"
						id="precioVenta"
						bind:value={formData.precioVenta}
						disabled={loading}
						placeholder="0.00"
					/>
				</div>

				<div class="form-group">
					<label for="stockMinimo">Stock Mínimo</label>
					<input
						type="number"
						min="0"
						id="stockMinimo"
						bind:value={formData.stockMinimo}
						disabled={loading}
						placeholder="0"
					/>
				</div>

				<div class="form-group">
					<label for="stockMaximo">Stock Máximo</label>
					<input
						type="number"
						min="0"
						id="stockMaximo"
						bind:value={formData.stockMaximo}
						disabled={loading}
						placeholder="0"
					/>
				</div>

				{#if !editMode}
				<div class="form-group">
					<label for="stockInicial">Stock Inicial</label>
					<input
						type="number"
						id="stockInicial"
						bind:value={formData.stockInicial}
						disabled={loading}
						placeholder="0"
					/>
				</div>
				{:else}
				<div class="form-group stock-adjustment">
					<label for="tipoAjuste">Tipo de Ajuste</label>
					<select id="tipoAjuste" bind:value={formData.tipoAjuste} disabled={loading}>
						<option value="suma">Sumar al stock actual</option>
						<option value="reemplazo">Reemplazar stock actual</option>
					</select>
				</div>

				<div class="form-group stock-adjustment">
					<label for="stockAjuste">
						{formData.tipoAjuste === 'suma' ? 'Cantidad a Sumar' : 'Nuevo Stock Total'}
					</label>
					<input
						type="number"
						id="stockAjuste"
						bind:value={formData.stockAjuste}
						disabled={loading}
						placeholder="0"
						min={formData.tipoAjuste === 'reemplazo' ? '0' : undefined}
					/>
					{#if formData.tipoAjuste === 'suma' && formData.stockAjuste !== 0}
					<small class="stock-preview">
						Stock actual: {productos.find(p => p.id === formData.id)?.stock_actual || 0}
						→ Nuevo stock: {(productos.find(p => p.id === formData.id)?.stock_actual || 0) + Number(formData.stockAjuste)}
					</small>
					{/if}
				</div>
				{/if}
			</div>

			{#if error}
			<div class="error">{error}</div>
			{/if}

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

	.search-bar {
		margin-bottom: 1.5rem;
	}

	.search-bar input {
		width: 100%;
		max-width: 400px;
		padding: 0.75rem;
		border: 2px solid #ddd;
		border-radius: 5px;
		font-size: 1rem;
	}

	.search-bar input:focus {
		outline: none;
		border-color: #667eea;
	}

	.table-container {
		background: white;
		border-radius: 10px;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
		overflow: hidden;
	}

	.products-table {
		width: 100%;
		border-collapse: collapse;
	}

	.products-table th {
		background-color: #f8f9fa;
		padding: 1rem;
		text-align: left;
		font-weight: 600;
		color: #333;
		border-bottom: 2px solid #dee2e6;
	}

	.products-table td {
		padding: 1rem;
		border-bottom: 1px solid #dee2e6;
		vertical-align: middle;
	}

	.products-table tr:hover {
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

	.stock-info {
		font-family: monospace;
		font-weight: 500;
	}

	.stock-badge {
		padding: 0.25rem 0.75rem;
		border-radius: 20px;
		font-size: 0.8rem;
		font-weight: 600;
		text-transform: uppercase;
	}

	.stock-badge.ok {
		background-color: #d1fae5;
		color: #065f46;
	}

	.stock-badge.bajo-stock {
		background-color: #fef3c7;
		color: #92400e;
	}

	.stock-badge.sin-stock {
		background-color: #fee2e2;
		color: #991b1b;
	}

	.actions {
		display: flex;
		gap: 0.5rem;
	}

	.btn-edit, .btn-delete {
		padding: 0.5rem;
		border: none;
		border-radius: 5px;
		cursor: pointer;
		font-size: 1rem;
		transition: background-color 0.2s;
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

	.text-center { text-align: center; }
	.text-right { text-align: right; }

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
		max-width: 800px;
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

	.form-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 1rem;
		padding: 1.5rem;
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
	}

	.form-group input,
	.form-group select,
	.form-group textarea {
		padding: 0.75rem;
		border: 2px solid #ddd;
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

	.form-group textarea {
		min-height: 100px;
		resize: vertical;
	}

	.error {
		background-color: #fee;
		color: #c33;
		padding: 0.75rem;
		border-radius: 5px;
		margin: 0 1.5rem;
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

	.empty-state {
		padding: 2rem;
		text-align: center;
		color: #666;
	}

	.stock-preview {
		color: #666;
		font-size: 0.9rem;
		margin-top: 0.5rem;
		font-style: italic;
	}

	.form-group.stock-adjustment {
		border: 2px solid #f0f9ff;
		background-color: #f8fafc;
		padding: 1rem;
		border-radius: 8px;
		margin-top: 0.5rem;
	}

	.form-group.stock-adjustment label {
		color: #0369a1;
		font-weight: 600;
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
		.page-header {
			flex-direction: column;
			gap: 1rem;
			align-items: stretch;
		}

		.form-grid {
			grid-template-columns: 1fr;
		}

		.products-table {
			font-size: 0.9rem;
		}

		.products-table th,
		.products-table td {
			padding: 0.5rem;
		}

		.actions {
			flex-direction: column;
		}
	}

	/* Estilos para imágenes */
	.product-image {
		width: 50px;
		height: 50px;
		object-fit: cover;
		border-radius: 5px;
		border: 1px solid #ddd;
	}

	.no-image {
		width: 50px;
		height: 50px;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #f8f9fa;
		border: 1px dashed #ddd;
		border-radius: 5px;
		font-size: 0.7rem;
		color: #666;
		text-align: center;
	}

	.file-help {
		color: #666;
		font-size: 0.8rem;
		margin-top: 0.25rem;
		display: block;
	}

	.image-preview {
		margin-top: 1rem;
		text-align: center;
	}

	.image-preview img {
		max-width: 200px;
		max-height: 200px;
		object-fit: contain;
		border: 1px solid #ddd;
		border-radius: 5px;
	}

	.btn-remove-image {
		display: block;
		margin: 0.5rem auto 0;
		background: #dc3545;
		color: white;
		border: none;
		padding: 0.5rem 1rem;
		border-radius: 3px;
		font-size: 0.9rem;
		cursor: pointer;
		transition: background-color 0.2s;
	}

	.btn-remove-image:hover {
		background: #c82333;
	}
</style>