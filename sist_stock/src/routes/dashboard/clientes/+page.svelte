<script>
  import { invalidate } from '$app/navigation';
  import { onMount, onDestroy } from 'svelte';
  import { notify } from '$lib/stores/notifications.js';
  import { saveItem, deleteItem } from '$lib/utils/dataOperations.js';
  import { setupAutoRefresh, cleanupAutoRefresh } from '$lib/utils/autoRefresh.js';

  export let data;
  let clientes = data.clientes || [];
  
  // Setup auto-refresh
  onMount(() => {
    setupAutoRefresh('clientes');
  });

  onDestroy(() => {
    cleanupAutoRefresh('clientes');
  });

  // Variables del modal
  let showModal = false;
  let editMode = false;
  let loading = false;
  let error = '';

  // Variables del formulario
  let formData = {
    id: '',
    nombre: '',
    documento: '',
    telefono: '',
    email: '',
    direccion: ''
  };

  // Variable de búsqueda
  let searchTerm = '';

  $: filteredClientes = clientes.filter(cliente =>
    cliente.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    cliente.documento?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    cliente.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  function openModal(cliente = null) {
    if (cliente) {
      editMode = true;
      formData = {
        id: cliente.id,
        nombre: cliente.nombre,
        documento: cliente.documento || '',
        telefono: cliente.telefono || '',
        email: cliente.email || '',
        direccion: cliente.direccion || ''
      };
    } else {
      editMode = false;
      formData = {
        id: '',
        nombre: '',
        documento: '',
        telefono: '',
        email: '',
        direccion: ''
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

  async function saveCliente() {
    if (!formData.nombre.trim()) {
      error = 'El nombre es obligatorio';
      return;
    }

    loading = true;
    error = '';

    try {
      const method = editMode ? 'PUT' : 'POST';
      const url = editMode ? `/api/clientes/${formData.id}` : '/api/clientes';
      
      await saveItem(
        async () => {
          const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
          });

          const result = await response.json();

          if (!response.ok) {
            throw new Error(result.error || 'Error al guardar el cliente');
          }
          
          return result;
        },
        editMode,
        'clientes',
        'Cliente'
      );
      
      closeModal();
    } catch (err) {
      error = err.message || 'Error de conexión';
    } finally {
      loading = false;
    }
  }

  async function deleteCliente(id, nombre) {
    try {
      await deleteItem(
        async () => {
          const response = await fetch(`/api/clientes/${id}`, {
            method: 'DELETE'
          });

          const result = await response.json();

          if (!response.ok) {
            throw new Error(result.error || 'Error al eliminar el cliente');
          }
          
          return result;
        },
        nombre,
        'clientes'
      );
    } catch (err) {
      // El error ya se maneja en deleteItem
      console.error('Error al eliminar cliente:', err);
    }
  }
</script>

<svelte:head>
  <title>Clientes - Sistema de Stock</title>
</svelte:head>

<div class="page-header">
  <h1>Gestión de Clientes</h1>
  <button class="btn-primary" on:click={() => openModal()}>
    ➕ Nuevo Cliente
  </button>
</div>

<div class="search-bar">
  <input
    type="text"
    placeholder="Buscar por nombre, documento o email..."
    bind:value={searchTerm}
  />
</div>

<div class="table-container">
  <table class="clients-table">
    <thead>
      <tr>
        <th>Nombre</th>
        <th>Documento</th>
        <th>Teléfono</th>
        <th>Email</th>
        <th>Fecha Registro</th>
        <th>Acciones</th>
      </tr>
    </thead>
    <tbody>
      {#each filteredClientes as cliente (cliente.id)}
      <tr>
        <td>
          <div class="client-name">{cliente.nombre}</div>
          {#if cliente.direccion}
          <div class="client-address">{cliente.direccion}</div>
          {/if}
        </td>
        <td>{cliente.documento || '-'}</td>
        <td>{cliente.telefono || '-'}</td>
        <td>{cliente.email || '-'}</td>
        <td>{new Date(cliente.created_at).toLocaleDateString()}</td>
        <td>
          <div class="actions">
            <button class="btn-edit" on:click={() => openModal(cliente)}>
              Editar
            </button>
            <button class="btn-delete" on:click={() => deleteCliente(cliente.id, cliente.nombre)}>
              Eliminar
            </button>
          </div>
        </td>
      </tr>
      {:else}
      <tr>
        <td colspan="6" class="text-center">No se encontraron clientes</td>
      </tr>
      {/each}
    </tbody>
  </table>
</div>

<!-- Modal -->
{#if showModal}
<div class="modal-overlay">
  <div class="modal">
    <div class="modal-header">
      <h2>{editMode ? 'Editar Cliente' : 'Nuevo Cliente'}</h2>
      <button class="btn-close" on:click={closeModal}>×</button>
    </div>

    <form on:submit|preventDefault={saveCliente}>
      <div class="form-grid">
        <div class="form-group">
          <label for="nombre">Nombre *</label>
          <input
            type="text"
            id="nombre"
            bind:value={formData.nombre}
            required
            disabled={loading}
            placeholder="Nombre completo del cliente"
          />
        </div>

        <div class="form-group">
          <label for="documento">Documento</label>
          <input
            type="text"
            id="documento"
            bind:value={formData.documento}
            disabled={loading}
            placeholder="DNI, CUIT, etc."
          />
        </div>

        <div class="form-group">
          <label for="telefono">Teléfono</label>
          <input
            type="text"
            id="telefono"
            bind:value={formData.telefono}
            disabled={loading}
            placeholder="Número de teléfono"
          />
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input
            type="email"
            id="email"
            bind:value={formData.email}
            disabled={loading}
            placeholder="correo@ejemplo.com"
          />
        </div>

        <div class="form-group full-width">
          <label for="direccion">Dirección</label>
          <textarea
            id="direccion"
            bind:value={formData.direccion}
            disabled={loading}
            placeholder="Dirección completa"
          ></textarea>
        </div>
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

  .clients-table {
    width: 100%;
    border-collapse: collapse;
  }

  .clients-table th {
    background-color: #f8f9fa;
    padding: 1rem;
    text-align: left;
    font-weight: 600;
    color: #333;
    border-bottom: 1px solid #dee2e6;
  }

  .clients-table td {
    padding: 1rem;
    border-bottom: 1px solid #f0f0f0;
    vertical-align: top;
  }

  .clients-table tr:hover {
    background-color: #f8f9fa;
  }

  .client-name {
    font-weight: 500;
    color: #333;
  }

  .client-address {
    font-size: 0.85rem;
    color: #666;
    margin-top: 0.25rem;
  }

  .actions {
    display: flex;
    gap: 0.5rem;
  }

  .btn-primary {
    background-color: #667eea;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 5px;
    cursor: pointer;
    font-weight: 500;
    transition: background-color 0.2s;
  }

  .btn-primary:hover {
    background-color: #5a67d8;
  }

  .btn-edit {
    background-color: #48bb78;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.875rem;
    transition: background-color 0.2s;
  }

  .btn-edit:hover {
    background-color: #38a169;
  }

  .btn-delete {
    background-color: #f56565;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.875rem;
    transition: background-color 0.2s;
  }

  .btn-delete:hover {
    background-color: #e53e3e;
  }

  /* Modal styles */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }

  .modal {
    background: white;
    border-radius: 10px;
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 1px solid #e2e8f0;
  }

  .modal-header h2 {
    margin: 0;
    color: #333;
  }

  .btn-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #666;
    padding: 0;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .btn-close:hover {
    color: #333;
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
    font-weight: 500;
    margin-bottom: 0.5rem;
    color: #333;
  }

  .form-group input,
  .form-group textarea {
    padding: 0.75rem;
    border: 2px solid #e2e8f0;
    border-radius: 5px;
    font-size: 1rem;
    transition: border-color 0.2s;
  }

  .form-group input:focus,
  .form-group textarea:focus {
    outline: none;
    border-color: #667eea;
  }

  .form-group input:disabled,
  .form-group textarea:disabled {
    background-color: #f7fafc;
    cursor: not-allowed;
  }

  .form-group textarea {
    min-height: 80px;
    resize: vertical;
  }

  .error {
    background-color: #fed7d7;
    color: #c53030;
    padding: 0.75rem;
    border-radius: 5px;
    margin: 0 1.5rem;
    font-size: 0.875rem;
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    padding: 1.5rem;
    border-top: 1px solid #e2e8f0;
  }

  .btn-cancel {
    background-color: #e2e8f0;
    color: #4a5568;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 5px;
    cursor: pointer;
    font-weight: 500;
    transition: background-color 0.2s;
  }

  .btn-cancel:hover {
    background-color: #cbd5e0;
  }

  .btn-save {
    background-color: #667eea;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 5px;
    cursor: pointer;
    font-weight: 500;
    transition: background-color 0.2s;
  }

  .btn-save:hover:not(:disabled) {
    background-color: #5a67d8;
  }

  .btn-save:disabled {
    background-color: #a0aec0;
    cursor: not-allowed;
  }

  .text-center {
    text-align: center;
    color: #666;
    font-style: italic;
  }

  @media (max-width: 768px) {
    .form-grid {
      grid-template-columns: 1fr;
    }

    .modal {
      width: 95%;
      margin: 1rem;
    }

    .clients-table {
      font-size: 0.875rem;
    }

    .actions {
      flex-direction: column;
    }
  }
</style>