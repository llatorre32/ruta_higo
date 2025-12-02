<script>
  import { onMount } from 'svelte';
  
  export let data;
  
  let loading = false;
  let error = '';
  let success = '';
  let showPasswordModal = false;
  
  // Formulario de perfil
  let profileData = {
    nombre: data.user.nombre,
    email: data.user.email
  };
  
  // Formulario de cambio de contraseña
  let passwordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  
  function openPasswordModal() {
    passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
    showPasswordModal = true;
    error = '';
    success = '';
  }
  
  function closePasswordModal() {
    showPasswordModal = false;
    error = '';
  }
  
  async function updateProfile() {
    loading = true;
    error = '';
    success = '';
    
    try {
      const response = await fetch(`/api/usuarios/${data.user.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(profileData)
      });
      
      const result = await response.json();
      
      if (response.ok) {
        success = 'Perfil actualizado correctamente';
        // Actualizar los datos locales
        data.user.nombre = profileData.nombre;
        data.user.email = profileData.email;
      } else {
        error = result.error || 'Error al actualizar perfil';
      }
    } catch (err) {
      error = 'Error de conexión';
      console.error('Error:', err);
    } finally {
      loading = false;
    }
  }
  
  async function changePassword() {
    error = '';
    success = '';
    
    try {
      const response = await fetch('/api/usuarios/cambiar-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(passwordData)
      });
      
      const result = await response.json();
      
      if (response.ok) {
        success = 'Contraseña cambiada correctamente';
        closePasswordModal();
      } else {
        error = result.error || 'Error al cambiar contraseña';
      }
    } catch (err) {
      error = 'Error de conexión';
      console.error('Error:', err);
    }
  }
  
  // Limpiar mensajes después de un tiempo
  $: if (success) {
    setTimeout(() => { success = ''; }, 5000);
  }
</script>

<svelte:head>
  <title>Mi Perfil - Sistema de Stock</title>
</svelte:head>

<div class="perfil-container">
  <div class="page-header">
    <h1>Mi Perfil</h1>
  </div>
  
  {#if error}
    <div class="alert alert-error">{error}</div>
  {/if}
  
  {#if success}
    <div class="alert alert-success">{success}</div>
  {/if}
  
  <div class="profile-grid">
    <!-- Información del usuario -->
    <div class="card">
      <div class="card-header">
        <h2>Información de la Cuenta</h2>
      </div>
      <div class="card-body">
        <div class="info-group">
          <label>Usuario:</label>
          <span class="username">{data.user.username}</span>
        </div>
        
        <div class="info-group">
          <label>Rol:</label>
          <span class="badge {data.user.rol === 'administrador' ? 'badge-admin' : 'badge-manager'}">
            {data.user.rol}
          </span>
        </div>
      </div>
    </div>
    
    <!-- Editar perfil -->
    <div class="card">
      <div class="card-header">
        <h2>Editar Perfil</h2>
      </div>
      <div class="card-body">
        <form on:submit|preventDefault={updateProfile}>
          <div class="form-group">
            <label for="nombre">Nombre completo</label>
            <input
              type="text"
              id="nombre"
              bind:value={profileData.nombre}
              required
            />
          </div>
          
          <div class="form-group">
            <label for="email">Email</label>
            <input
              type="email"
              id="email"
              bind:value={profileData.email}
              required
            />
          </div>
          
          <button type="submit" class="btn btn-primary" disabled={loading}>
            {loading ? 'Actualizando...' : 'Actualizar Perfil'}
          </button>
        </form>
      </div>
    </div>
    
    <!-- Cambiar contraseña -->
    <div class="card">
      <div class="card-header">
        <h2>Seguridad</h2>
      </div>
      <div class="card-body">
        <p>Mantén tu cuenta segura cambiando tu contraseña regularmente.</p>
        <button class="btn btn-secondary" on:click={openPasswordModal}>
          Cambiar Contraseña
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Modal de cambio de contraseña -->
{#if showPasswordModal}
  <div class="modal-overlay" on:click={closePasswordModal}>
    <div class="modal" on:click|stopPropagation>
      <div class="modal-header">
        <h2>Cambiar Contraseña</h2>
        <button class="btn-close" on:click={closePasswordModal}>&times;</button>
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
            <small>Mínimo 6 caracteres</small>
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
          <button type="button" class="btn btn-secondary" on:click={closePasswordModal}>
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

<style>
  .perfil-container {
    padding: 2rem;
    max-width: 1000px;
    margin: 0 auto;
  }
  
  .page-header {
    margin-bottom: 2rem;
  }
  
  .page-header h1 {
    margin: 0;
    color: #333;
    font-size: 2rem;
    font-weight: 600;
  }
  
  .profile-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  @media (min-width: 768px) {
    .profile-grid {
      grid-template-columns: 1fr 1fr;
    }
  }
  
  .card {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .card-header {
    padding: 1.5rem;
    border-bottom: 1px solid #eee;
  }
  
  .card-header h2 {
    margin: 0;
    font-size: 1.25rem;
    font-weight: 600;
    color: #333;
  }
  
  .card-body {
    padding: 1.5rem;
  }
  
  .info-group {
    display: flex;
    align-items: center;
    margin-bottom: 1rem;
    gap: 1rem;
  }
  
  .info-group label {
    font-weight: 500;
    color: #666;
    min-width: 60px;
  }
  
  .username {
    font-family: monospace;
    font-weight: 600;
    background-color: #f8f9fa;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
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
  
  .form-group {
    margin-bottom: 1.5rem;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
  }
  
  .form-group input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 1rem;
  }
  
  .form-group input:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
  }
  
  .form-group small {
    color: #666;
    font-size: 0.875rem;
    margin-top: 0.25rem;
    display: block;
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
  
  .btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  .btn-primary {
    background-color: #007bff;
    color: white;
  }
  
  .btn-primary:hover:not(:disabled) {
    background-color: #0056b3;
  }
  
  .btn-secondary {
    background-color: #6c757d;
    color: white;
  }
  
  .btn-secondary:hover {
    background-color: #545b62;
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
  
  .alert-success {
    background-color: #dcfce7;
    color: #16a34a;
    border: 1px solid #a7f3d0;
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
  
  @media (max-width: 768px) {
    .perfil-container {
      padding: 1rem;
    }
    
    .profile-grid {
      grid-template-columns: 1fr;
    }
    
    .card-header,
    .card-body {
      padding: 1rem;
    }
    
    .info-group {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
  }
</style>