import { invalidate } from '$app/navigation';
import { notify } from '$lib/stores/notifications.js';

// Helper para refrescar datos específicos
export async function refreshData(type, showNotification = false) {
  try {
    await invalidate(`app:${type}`);
    if (showNotification) {
      notify.info('Datos actualizados correctamente');
    }
  } catch (error) {
    console.error(`Error al refrescar ${type}:`, error);
    if (showNotification) {
      notify.error('Error al actualizar los datos');
    }
  }
}

// Helper para operaciones CRUD con notificaciones y refresh automático
export async function performOperation(operation, dataType, options = {}) {
  const {
    successMessage,
    errorMessage,
    showSuccessNotification = true,
    showErrorNotification = true,
    autoRefresh = true
  } = options;

  try {
    const result = await operation();
    
    if (result.ok || result.success !== false) {
      if (showSuccessNotification && successMessage) {
        notify.success(successMessage);
      }
      
      if (autoRefresh && dataType) {
        await refreshData(dataType);
      }
      
      return result;
    } else {
      throw new Error(result.error || errorMessage || 'Error en la operación');
    }
  } catch (error) {
    console.error('Error en operación:', error);
    
    if (showErrorNotification) {
      const message = error.message || errorMessage || 'Error en la operación';
      notify.error(message);
    }
    
    throw error;
  }
}

// Helper específico para eliminar elementos
export async function deleteItem(deleteFunction, itemName, dataType) {
  if (!confirm(`¿Está seguro de eliminar "${itemName}"?`)) {
    return false;
  }

  return performOperation(
    deleteFunction,
    dataType,
    {
      successMessage: `${itemName} eliminado correctamente`,
      errorMessage: 'Error al eliminar el elemento'
    }
  );
}

// Helper específico para guardar elementos
export async function saveItem(saveFunction, isEdit, dataType, itemName = 'elemento') {
  const action = isEdit ? 'actualizado' : 'creado';
  
  return performOperation(
    saveFunction,
    dataType,
    {
      successMessage: `${itemName} ${action} correctamente`,
      errorMessage: `Error al ${isEdit ? 'actualizar' : 'crear'} el ${itemName.toLowerCase()}`
    }
  );
}