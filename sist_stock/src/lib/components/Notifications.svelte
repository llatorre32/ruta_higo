<script>
  import { notifications, removeNotification } from '$lib/stores/notifications.js';
  import { fly, fade } from 'svelte/transition';

  function getIcon(type) {
    switch (type) {
      case 'success': return '✅';
      case 'error': return '❌';
      case 'warning': return '⚠️';
      case 'info': return 'ℹ️';
      default: return 'ℹ️';
    }
  }
</script>

<div class="notifications-container">
  {#each $notifications as notification (notification.id)}
    <div
      class="notification notification-{notification.type}"
      in:fly={{ x: 300, duration: 300 }}
      out:fade={{ duration: 200 }}
    >
      <div class="notification-content">
        <span class="notification-icon">
          {getIcon(notification.type)}
        </span>
        <span class="notification-message">
          {notification.message}
        </span>
        <button
          class="notification-close"
          on:click={() => removeNotification(notification.id)}
          aria-label="Cerrar notificación"
        >
          ✕
        </button>
      </div>
    </div>
  {/each}
</div>

<style>
  .notifications-container {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    gap: 10px;
    max-width: 400px;
  }

  .notification {
    min-width: 300px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    border-left: 4px solid;
  }

  .notification-content {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: white;
    gap: 12px;
  }

  .notification-icon {
    font-size: 1.2rem;
    flex-shrink: 0;
  }

  .notification-message {
    flex: 1;
    font-size: 0.95rem;
    line-height: 1.4;
    color: #333;
  }

  .notification-close {
    background: none;
    border: none;
    font-size: 1.1rem;
    cursor: pointer;
    color: #666;
    padding: 2px;
    border-radius: 50%;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .notification-close:hover {
    background-color: rgba(0, 0, 0, 0.1);
    color: #333;
  }

  /* Notification types */
  .notification-success {
    border-left-color: #10b981;
  }

  .notification-error {
    border-left-color: #ef4444;
  }

  .notification-warning {
    border-left-color: #f59e0b;
  }

  .notification-info {
    border-left-color: #3b82f6;
  }

  /* Mobile responsiveness */
  @media (max-width: 480px) {
    .notifications-container {
      top: 10px;
      right: 10px;
      left: 10px;
      max-width: none;
    }

    .notification {
      min-width: auto;
    }

    .notification-content {
      padding: 10px 12px;
    }

    .notification-message {
      font-size: 0.9rem;
    }
  }
</style>