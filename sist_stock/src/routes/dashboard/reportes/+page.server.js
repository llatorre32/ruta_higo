import { 
  getProductosBajoStock, 
  getResumenStock, 
  getMovimientosStock,
  getReporteVentasMensual,
  getReporteVentasPorVendedor,
  getReporteProductosMasVendidos
} from '$lib/stock.js';

export function load() {
  try {
    const productosBajoStock = getProductosBajoStock();
    const resumen = getResumenStock();
    const ultimosMovimientos = getMovimientosStock(null, 20);
    
    // Reportes de ventas
    const ventasMensuales = getReporteVentasMensual();
    const ventasPorVendedor = getReporteVentasPorVendedor();
    const productosMasVendidos = getReporteProductosMasVendidos();
    
    return {
      productosBajoStock,
      resumen,
      ultimosMovimientos,
      ventasMensuales,
      ventasPorVendedor,
      productosMasVendidos
    };
  } catch (error) {
    console.error('Error al cargar reportes:', error);
    return {
      productosBajoStock: [],
      resumen: {
        totalProductos: 0,
        productosBajoStock: 0,
        valorInventario: 0,
        totalCategorias: 0
      },
      ultimosMovimientos: [],
      ventasMensuales: [],
      ventasPorVendedor: [],
      productosMasVendidos: []
    };
  }
}