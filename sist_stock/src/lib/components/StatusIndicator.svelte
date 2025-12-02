<script>
  import { notifications } from '$lib/stores/notifications.js';
  import { onMount, onDestroy } from 'svelte';

  let connectionStatus = 'online';
  let showStatus = false;
  let activeOperations = 0;

  // Monitor el estado de conexión
  onMount(() => {
    const updateOnlineStatus = () => {
      const newStatus = navigator.onLine ? 'online' : 'offline';
      if (newStatus !== connectionStatus) {
        connectionStatus = newStatus;
        showStatus = true;
        setTimeout(() => { showStatus = false; }, 3000);
      }
    };

    window.addEventListener('online', updateOnlineStatus);
    window.addEventListener('offline', updateOnlineStatus);
    
    return () => {
      window.removeEventListener('online', updateOnlineStatus);
      window.removeEventListener('offline', updateOnlineStatus);
    };
  });

  // Monitor notificaciones activas para mostrar indicador de carga
  $: {
    activeOperations = $notifications.filter(n => n.type === 'info' && n.message.includes('...')).length;
  }
</script>

{#if showStatus || activeOperations > 0}
<div class="status-indicator" class:offline={connectionStatus === 'offline'}>
  {#if connectionStatus === 'offline'}
    <span class="status-icon">🔴</span>
    <span>Sin conexión</span>
  {:else if activeOperations > 0}
    <span class="status-icon loading">⚡</span>
    <span>Sincronizando datos...</span>
  {:else}
    <span class="status-icon">🟢</span>
    <span>Conectado</span>
  {/if}
</div>
{/if}

<style>
  .status-indicator {
    position: fixed;
    bottom: 20px;
    left: 20px;
    background: white;
    border: 2px solid #10b981;
    border-radius: 25px;
    padding: 8px 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 0.9rem;
    font-weight: 500;
    color: #10b981;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    z-index: 999;
    transition: all 0.3s ease;
  }

  .status-indicator.offline {
    border-color: #ef4444;
    color: #ef4444;
  }

  .status-icon {
    font-size: 1rem;
  }

  .status-icon.loading {
    animation: pulse 1.5s ease-in-out infinite;
  }

  @keyframes pulse {
    0%, 100% { transform: scale(1); opacity: 1; }
    50% { transform: scale(1.1); opacity: 0.7; }
  }

  @media (max-width: 768px) {
    .status-indicator {
      bottom: 10px;
      left: 10px;
      font-size: 0.8rem;
      padding: 6px 12px;
    }
  }
</style>