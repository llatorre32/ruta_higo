import { browser } from '$app/environment';
import { refreshData } from './dataOperations.js';

// Auto-refresh configuración por página
const AUTO_REFRESH_CONFIG = {
  'productos': {
    interval: 30000, // 30 segundos
    onVisibilityChange: true
  },
  'usuarios': {
    interval: 60000, // 1 minuto
    onVisibilityChange: false
  },
  'movimientos': {
    interval: 15000, // 15 segundos
    onVisibilityChange: true
  },
  'ventas': {
    interval: 30000, // 30 segundos
    onVisibilityChange: true
  },
  'categorias': {
    interval: 120000, // 2 minutos
    onVisibilityChange: false
  },
  'clientes': {
    interval: 120000, // 2 minutos
    onVisibilityChange: false
  }
};

let refreshIntervals = new Map();
let visibilityHandlers = new Map();

/**
 * Configura el auto-refresh para un tipo de datos específico
 * @param {string} dataType - Tipo de datos (productos, usuarios, etc.)
 * @param {Object} options - Opciones de configuración
 */
export function setupAutoRefresh(dataType, options = {}) {
  if (!browser) return;

  const config = {
    ...AUTO_REFRESH_CONFIG[dataType],
    ...options
  };

  // Limpiar cualquier configuración anterior
  cleanupAutoRefresh(dataType);

  // Configurar interval si está especificado
  if (config.interval && config.interval > 0) {
    const intervalId = setInterval(() => {
      refreshData(dataType, false); // Sin notificación para auto-refresh
    }, config.interval);
    
    refreshIntervals.set(dataType, intervalId);
  }

  // Configurar refresh al cambiar visibilidad de la página
  if (config.onVisibilityChange) {
    const handler = () => {
      if (!document.hidden) {
        refreshData(dataType, false); // Sin notificación para auto-refresh
      }
    };
    
    document.addEventListener('visibilitychange', handler);
    visibilityHandlers.set(dataType, handler);
  }
}

/**
 * Limpia la configuración de auto-refresh para un tipo de datos
 * @param {string} dataType - Tipo de datos
 */
export function cleanupAutoRefresh(dataType) {
  if (!browser) return;

  // Limpiar interval
  const intervalId = refreshIntervals.get(dataType);
  if (intervalId) {
    clearInterval(intervalId);
    refreshIntervals.delete(dataType);
  }

  // Limpiar event listener
  const handler = visibilityHandlers.get(dataType);
  if (handler) {
    document.removeEventListener('visibilitychange', handler);
    visibilityHandlers.delete(dataType);
  }
}

/**
 * Limpia todas las configuraciones de auto-refresh
 */
export function cleanupAllAutoRefresh() {
  if (!browser) return;

  // Limpiar todos los intervals
  for (const intervalId of refreshIntervals.values()) {
    clearInterval(intervalId);
  }
  refreshIntervals.clear();

  // Limpiar todos los event listeners
  for (const handler of visibilityHandlers.values()) {
    document.removeEventListener('visibilitychange', handler);
  }
  visibilityHandlers.clear();
}

/**
 * Pausa temporalmente el auto-refresh para un tipo de datos
 * @param {string} dataType - Tipo de datos
 */
export function pauseAutoRefresh(dataType) {
  const intervalId = refreshIntervals.get(dataType);
  if (intervalId) {
    clearInterval(intervalId);
    refreshIntervals.delete(dataType);
  }
}

/**
 * Reanuda el auto-refresh para un tipo de datos
 * @param {string} dataType - Tipo de datos
 */
export function resumeAutoRefresh(dataType) {
  if (!browser) return;

  const config = AUTO_REFRESH_CONFIG[dataType];
  if (config && config.interval && !refreshIntervals.has(dataType)) {
    const intervalId = setInterval(() => {
      refreshData(dataType, false);
    }, config.interval);
    
    refreshIntervals.set(dataType, intervalId);
  }
}