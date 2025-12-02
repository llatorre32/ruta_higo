import { writable } from 'svelte/store';

export const notifications = writable([]);

let nextId = 1;

export function addNotification(type, message, duration = 5000) {
  const id = nextId++;
  const notification = {
    id,
    type, // 'success', 'error', 'warning', 'info'
    message,
    duration
  };

  notifications.update(items => [...items, notification]);

  // Auto remove notification after duration
  if (duration > 0) {
    setTimeout(() => {
      removeNotification(id);
    }, duration);
  }

  return id;
}

export function removeNotification(id) {
  notifications.update(items => items.filter(item => item.id !== id));
}

export function clearNotifications() {
  notifications.set([]);
}

// Helper functions for specific notification types
export const notify = {
  success: (message, duration) => addNotification('success', message, duration),
  error: (message, duration) => addNotification('error', message, duration),
  warning: (message, duration) => addNotification('warning', message, duration),
  info: (message, duration) => addNotification('info', message, duration)
};