<script>
  import { onMount, onDestroy } from 'svelte';
  import { page } from '$app/stores';
  import { notify } from '$lib/stores/notifications.js';
  import { saveItem, deleteItem } from '$lib/utils/dataOperations.js';
  import { setupAutoRefresh, cleanupAutoRefresh } from '$lib/utils/autoRefresh.js';
  
  export let data;
  
  let usuarios = [];
  let loading = true;
  let error = '';
  let showModal = false;
  let showDeleteModal = false;
  let showPasswordModal = false;
  let editingUser = null;
  let deletingUser = null;
  
  // Formulario de usuario
  let formData = {
    username: '',
    password: '',
    nombre: '',
    email: '',
    rol: 'manejador'
  };
  
  // Formulario de cambio de contraseña
  let passwordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  
  // Paginación y búsqueda
  let currentPage = 1;
  let totalPages = 1;
  let searchTerm = '';
  let searchTimeout;
  
  onMount(() => {
    loadUsuarios();
    setupAutoRefresh('usuarios');
  });
  
  onDestroy(() => {
    cleanupAutoRefresh('usuarios');
  });
  
  async function loadUsuarios() {
    loading = true;
    error = '';
    
    try {
      const params = new URLSearchParams({
        page: currentPage.toString(),
        limit: '10',
        search: searchTerm
      });
      
      const response = await fetch(`/api/usuarios?${params}`);
      const result = await response.json();
      
      if (response.ok) {
        usuarios = result.usuarios;
        totalPages = result.pagination.pages;
      } else {
        error = result.error || 'Error al cargar usuarios';
      }
    } catch (err) {
      error = 'Error de conexión';
      console.error('Error:', err);
    } finally {
      loading = false;
    }
  }
  
  function openCreateModal() {
    editingUser = null;
    formData = {
      username: '',
      password: '',
      nombre: '',
      email: '',
      rol: 'manejador'
    };
    showModal = true;
  }
  
  function openEditModal(user) {
    editingUser = user;
    formData = {
      username: user.username,
      password: '',
      nombre: user.nombre,
      email: user.email,
      rol: user.rol
    };
    showModal = true;
  }
  
  function openPasswordModal() {
    passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    showPasswordModal = true;
  }
  
  function openDeleteModal(user) {
    deletingUser = user;
    showDeleteModal = true;
  }
  
  function closeModals() {
    showModal = false;
    showDeleteModal = false;
    showPasswordModal = false;
    editingUser = null;
    deletingUser = null;
    error = '';
  }
  
  async function saveUser() {
    error = '';
    
    try {
      const url = editingUser ? `/api/usuarios/${editingUser.id}` : '/api/usuarios';
      const method = editingUser ? 'PUT' : 'POST';
      
      const payload = { ...formData };
      if (editingUser && !payload.password) {
        delete payload.password; // No enviar contraseña vacía en edición
      }
      
      await saveItem(
        async () => {
          const response = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          
          const result = await response.json();
          
          if (!response.ok) {
            throw new Error(result.error || 'Error al guardar usuario');
          }
          
          return result;
        },
        !!editingUser,
        null, // No usar invalidate automático porque usamos loadUsuarios manual
        'Usuario'
      );
      
      closeModals();
      loadUsuarios(); // Refresh manual de la lista
    } catch (err) {
      error = err.message || 'Error de conexión';
    }
  }
  
  async function deleteUser() {
    error = '';
    
    if (!confirm(`¿Está seguro de eliminar al usuario "${deletingUser.nombre}"?`)) {
      return;
    }
    
    try {
      await deleteItem(
        async () => {
          const response = await fetch(`/api/usuarios/${deletingUser.id}`, {
            method: 'DELETE'
          });
          
          const result = await response.json();
          
          if (!response.ok) {
            throw new Error(result.error || 'Error al eliminar usuario');
          }
          
          return result;
        },
        deletingUser.nombre,
        null // No usar invalidate automático porque usamos loadUsuarios manual
      );
      
      closeModals();
      loadUsuarios(); // Refresh manual de la lista
    } catch (err) {
      error = err.message || 'Error de conexión';
    }
  }
  
  async function changePassword() {
    error = '';
    
    try {
      const response = await fetch('/api/usuarios/cambiar-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(passwordData)
      });
      
      const result = await response.json();
      
      if (response.ok) {
        closeModals();
        notify.success('Contraseña cambiada correctamente');
      } else {
        error = result.error || 'Error al cambiar contraseña';
      }
    } catch (err) {
      error = 'Error de conexión';
      console.error('Error:', err);
    }
  }
  
  function handleSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      currentPage = 1;
      loadUsuarios();
    }, 500);
  }
  
  function changePage(page) {
    currentPage = page;
    loadUsuarios();
  }
  
  function getRoleBadgeClass(rol) {
    return rol === 'administrador' ? 'badge-admin' : 'badge-manager';
  }
  
  function getStatusBadgeClass(activo) {
    return activo ? 'badge-active' : 'badge-inactive';
  }
</script>

<svelte:head>
  <title>Gestión de Usuarios - Sistema de Stock</title>
</svelte:head>

<div class="usuarios-container">
  <div class="page-header">
    <h1>Gestión de Usuarios</h1>
    <div class="header-actions">
      <button class="btn btn-secondary" on:click={openPasswordModal}>
        Cambiar mi contraseña
      </button>
      <button class="btn btn-primary" on:click={openCreateModal}>
        + Nuevo Usuario
      </button>
    </div>
  </div>
  
  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}
  
  <!-- Búsqueda -->
  <div class="search-section">
    <input
      type="text"
      placeholder="Buscar usuarios..."
      bind:value={searchTerm}
      on:input={handleSearch}
      class="search-input"
    />
  </div>
  
  {#if loading}
    <div class="loading">Cargando usuarios...</div>
  {:else if usuarios.length === 0}
    <div class="empty-state">
      <p>No se encontraron usuarios</p>
    </div>
  {:else}
    <!-- Tabla de usuarios -->
    <div class="table-container">
      <table class="usuarios-table">
        <thead>
          <tr>
            <th>Usuario</th>
            <th>Nombre</th>
            <th>Email</th>
            <th>Rol</th>
            <th>Estado</th>
            <th>Fecha Creación</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {#each usuarios as usuario}
            <tr>
              <td class="username">{usuario.username}</td>
              <td>{usuario.nombre}</td>
              <td>{usuario.email}</td>
              <td>
                <span class="badge {getRoleBadgeClass(usuario.rol)}">
                  {usuario.rol}
                </span>
              </td>
              <td>
                <span class="badge {getStatusBadgeClass(usuario.activo)}">
                  {usuario.activo ? 'Activo' : 'Inactivo'}
                </span>
              </td>
              <td>{new Date(usuario.created_at).toLocaleDateString()}</td>
              <td class="actions">
                <button 
                  class="btn btn-sm btn-secondary"
                  on:click={() => openEditModal(usuario)}
                >
                  Editar
                </button>
                {#if usuario.id !== data.user.id}
                  <button 
                    class="btn btn-sm btn-danger"
                    on:click={() => openDeleteModal(usuario)}
                  >
                    Eliminar
                  </button>
                {/if}
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
    
    <!-- Paginación -->
    {#if totalPages > 1}
      <div class="pagination">
        {#each Array(totalPages) as _, i}
          <button
            class="page-btn {currentPage === i + 1 ? 'active' : ''}"
            on:click={() => changePage(i + 1)}
          >
            {i + 1}
          </button>
        {/each}
      </div>
    {/if}
  {/if}
</div>

<!-- Modal de usuario -->
{#if showModal}
  <div class="modal-overlay" on:click={closeModals}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal-header">
        <h2>{editingUser ? 'Editar Usuario' : 'Nuevo Usuario'}</h2>
        <button class="btn-close" on:click={closeModals}>&times;</button>
      </div>
      
      <form on:submit|preventDefault={saveUser}>
        <div class="modal-body">
          {#if error}
            <div class="alert alert-error">{error}</div>
          {/if}
          
          <div class="form-group">
            <label for="username">Usuario</label>
            <input
              type="text"
              id="username"
              bind:value={formData.username}
              required
              disabled={!!editingUser}
            />
          </div>
          
          <div class="form-group">
            <label for="nombre">Nombre completo</label>
            <input
              type="text"
              id="nombre"
              bind:value={formData.nombre}
              required
            />
          </div>
          
          <div class="form-group">
            <label for="email">Email</label>
            <input
              type="email"
              id="email"
              bind:value={formData.email}
              required
            />
          </div>
          
          <div class="form-group">
            <label for="rol">Rol</label>
            <select id="rol" bind:value={formData.rol} required>
              <option value="manejador">Manejador</option>
              <option value="administrador">Administrador</option>
            </select>
          </div>
          
          {#if !editingUser}
            <div class="form-group">
              <label for="password">Contraseña</label>
              <input
                type="password"
                id="password"
                bind:value={formData.password}
                required
                minlength="6"
              />
            </div>
          {/if}
        </div>
        
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" on:click={closeModals}>
            Cancelar
          </button>
          <button type="submit" class="btn btn-primary">
            {editingUser ? 'Actualizar' : 'Crear'}
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

<!-- Modal de cambio de contraseña -->
{#if showPasswordModal}
  <div class="modal-overlay" on:click={closeModals}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal-header">
        <h2>Cambiar Contraseña</h2>
        <button class="btn-close" on:click={closeModals}>&times;</button>
      </div>
      
      <form on:submit|preventDefault={changePassword}>
        <div class="modal-body">
          {#if error}
            <div class="alert alert-error">{error}</div>
          {/if}
          
          <div class="form-group">
            <label for="currentPassword">Contraseña actual</label>
            <input
              type="password"
              id="currentPassword"
              bind:value={passwordData.currentPassword}
              required
            />
          </div>
          
          <div class="form-group">
            <label for="newPassword">Nueva contraseña</label>
            <input
              type="password"
              id="newPassword"
              bind:value={passwordData.newPassword}
              required
              minlength="6"
            />
          </div>
          
          <div class="form-group">
            <label for="confirmPassword">Confirmar nueva contraseña</label>
            <input
              type="password"
              id="confirmPassword"
              bind:value={passwordData.confirmPassword}
              required
              minlength="6"
            />
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" on:click={closeModals}>
            Cancelar
          </button>
          <button type="submit" class="btn btn-primary">
            Cambiar Contraseña
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

<!-- Modal de confirmación de eliminación -->
{#if showDeleteModal}
  <div class="modal-overlay" on:click={closeModals}>
    <div class="modal modal-small" on:click|stopPropagation>
      <div class="modal-header">
        <h2>Confirmar Eliminación</h2>
        <button class="btn-close" on:click={closeModals}>&times;</button>
      </div>
      
      <div class="modal-body">
        {#if error}
          <div class="alert alert-error">{error}</div>
        {/if}
        
        <p>¿Estás seguro de que deseas eliminar al usuario <strong>{deletingUser?.nombre}</strong>?</p>
        <p class="warning">Esta acción desactivará al usuario y no se puede deshacer.</p>
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" on:click={closeModals}>
          Cancelar
        </button>
        <button type="button" class="btn btn-danger" on:click={deleteUser}>
          Eliminar
        </button>
      </div>
    </div>
  </div>
{/if}

<style>
  .usuarios-container {
    padding: 2rem;
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .header-actions {
    display: flex;
    gap: 1rem;
  }
  
  .search-section {
    margin-bottom: 2rem;
  }
  
  .search-input {
    width: 100%;
    max-width: 400px;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 1rem;
  }
  
  .table-container {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    overflow-x: auto;
  }
  
  .usuarios-table {
    width: 100%;
    border-collapse: collapse;
  }
  
  .usuarios-table th,
  .usuarios-table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid #eee;
  }
  
  .usuarios-table th {
    background-color: #f8f9fa;
    font-weight: 600;
    color: #333;
  }
  
  .username {
    font-family: monospace;
    font-weight: 600;
  }
  
  .badge {
    padding: 0.25rem 0.75rem;
    border-radius: 12px;
    font-size: 0.875rem;
    font-weight: 500;
  }
  
  .badge-admin {
    background-color: #fef3c7;
    color: #d97706;
  }
  
  .badge-manager {
    background-color: #dbeafe;
    color: #2563eb;
  }
  
  .badge-active {
    background-color: #dcfce7;
    color: #16a34a;
  }
  
  .badge-inactive {
    background-color: #fee2e2;
    color: #dc2626;
  }
  
  .actions {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    gap: 0.5rem;
    margin-top: 2rem;
  }
  
  .page-btn {
    padding: 0.5rem 0.75rem;
    border: 1px solid #ddd;
    background: white;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .page-btn:hover {
    background-color: #f8f9fa;
  }
  
  .page-btn.active {
    background-color: #007bff;
    color: white;
    border-color: #007bff;
  }
  
  .loading,
  .empty-state {
    text-align: center;
    padding: 3rem;
    color: #666;
  }
  
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }
  
  .modal {
    background: white;
    border-radius: 8px;
    width: 90%;
    max-width: 500px;
    max-height: 90vh;
    overflow-y: auto;
  }
  
  .modal-small {
    max-width: 400px;
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 1px solid #eee;
  }
  
  .modal-header h2 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
  }
  
  .btn-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    padding: 0;
    width: 2rem;
    height: 2rem;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .modal-body {
    padding: 1.5rem;
  }
  
  .modal-footer {
    padding: 1.5rem;
    border-top: 1px solid #eee;
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
  }
  
  .form-group {
    margin-bottom: 1.5rem;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
  }
  
  .form-group input,
  .form-group select {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 1rem;
  }
  
  .form-group input:focus,
  .form-group select:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  }
  
  .form-group input:disabled {
    background-color: #f8f9fa;
    color: #666;
  }
  
  .warning {
    color: #d97706;
    font-size: 0.875rem;
    margin-top: 0.5rem;
  }
  
  .btn {
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    transition: all 0.2s;
  }
  
  .btn-primary {
    background-color: #007bff;
    color: white;
  }
  
  .btn-primary:hover {
    background-color: #0056b3;
  }
  
  .btn-secondary {
    background-color: #6c757d;
    color: white;
  }
  
  .btn-secondary:hover {
    background-color: #545b62;
  }
  
  .btn-danger {
    background-color: #dc3545;
    color: white;
  }
  
  .btn-danger:hover {
    background-color: #c82333;
  }
  
  .btn-sm {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
  }
  
  .alert {
    padding: 1rem;
    margin-bottom: 1rem;
    border-radius: 6px;
  }
  
  .alert-error {
    background-color: #fee2e2;
    color: #dc2626;
    border: 1px solid #fca5a5;
  }
  
  @media (max-width: 768px) {
    .usuarios-container {
      padding: 1rem;
    }
    
    .page-header {
      flex-direction: column;
      align-items: stretch;
    }
    
    .header-actions {
      justify-content: center;
    }
    
    .table-container {
      font-size: 0.875rem;
    }
    
    .usuarios-table th,
    .usuarios-table td {
      padding: 0.75rem 0.5rem;
    }
    
    .actions {
      flex-direction: column;
    }
    
    .btn-sm {
      font-size: 0.75rem;
      padding: 0.4rem 0.8rem;
    }
  }
</style>