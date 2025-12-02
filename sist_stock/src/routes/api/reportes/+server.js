import { json } from '@sveltejs/kit';
import { createReporte, getReportes, getReportesVentas } from '$lib/stock.js';

export async function GET({ url }) {
  try {
    const tipo = url.searchParams.get('tipo');
    
    if (tipo === 'ventas') {
      const fechaInicio = url.searchParams.get('fechaInicio') || new Date(Date.now() - 30*24*60*60*1000).toISOString().split('T')[0];
      const fechaFin = url.searchParams.get('fechaFin') || new Date().toISOString().split('T')[0];
      
      const reportes = getReportesVentas(fechaInicio, fechaFin);
      return json(reportes);
    }
    
    const reportes = getReportes();
    return json(reportes);
  } catch (error) {
    console.error('Error al obtener reportes:', error);
    return json({ error: 'Error al obtener reportes' }, { status: 500 });
  }
}

export async function POST({ request, locals }) {
  try {
    if (!locals.user) {
      return json({ error: 'No autorizado' }, { status: 401 });
    }

    // Solo administradores pueden crear reportes personalizados
    if (locals.user.rol !== 'administrador') {
      return json({ error: 'Permisos insuficientes' }, { status: 403 });
    }

    const data = await request.json();
    const { nombre, descripcion, tipo, query } = data;

    if (!nombre || !tipo || !query) {
      return json({ error: 'Faltan campos obligatorios' }, { status: 400 });
    }

    const resultado = createReporte(data, locals.user.id);
    return json(resultado);
  } catch (error) {
    console.error('Error al crear reporte:', error);
    return json({ error: error.message || 'Error al crear reporte' }, { status: 500 });
  }
}