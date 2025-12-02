package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.dto.request.CrearProductoRequest;
import com.cisasmendi.sistemastock.dto.request.ActualizarProductoRequest;
import com.cisasmendi.sistemastock.dto.response.ProductoResponse;
import com.cisasmendi.sistemastock.model.Producto;
import com.cisasmendi.sistemastock.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional
    public ProductoResponse crearProducto(CrearProductoRequest request, Long usuarioId) {
        if (productoRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe un producto con ese nombre");
        }

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 0);

        producto = productoRepository.save(producto);

        // Registrar auditoría
        auditoriaService.registrar(usuarioId, "CREAR_PRODUCTO", "Producto", 
                producto.getId().toString(), "Producto creado: " + producto.getNombre(), null);

        return convertirAResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizarProducto(UUID productoId, ActualizarProductoRequest request, Long usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (request.getNombre() != null && !request.getNombre().equals(producto.getNombre())) {
            if (productoRepository.existsByNombre(request.getNombre())) {
                throw new RuntimeException("Ya existe un producto con ese nombre");
            }
            producto.setNombre(request.getNombre());
        }

        if (request.getDescripcion() != null) {
            producto.setDescripcion(request.getDescripcion());
        }

        if (request.getPrecio() != null) {
            producto.setPrecio(request.getPrecio());
        }

        if (request.getStockActual() != null) {
            producto.setStockActual(request.getStockActual());
        }

        if (request.getStockMinimo() != null) {
            producto.setStockMinimo(request.getStockMinimo());
        }

        producto = productoRepository.save(producto);

        // Registrar auditoría
        auditoriaService.registrar(usuarioId, "ACTUALIZAR_PRODUCTO", "Producto", 
                producto.getId().toString(), "Producto actualizado: " + producto.getNombre(), null);

        return convertirAResponse(producto);
    }

    @Transactional
    public void eliminarProducto(UUID productoId, Long usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        String nombreProducto = producto.getNombre();
        productoRepository.delete(producto);

        // Registrar auditoría
        auditoriaService.registrar(usuarioId, "ELIMINAR_PRODUCTO", "Producto", 
                productoId.toString(), "Producto eliminado: " + nombreProducto, null);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProducto(UUID productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return convertirAResponse(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerTodosProductos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerProductosDisponibles() {
        List<Producto> productos = productoRepository.findProductosDisponibles();
        return productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductos(String busqueda) {
        List<Producto> productos = productoRepository.buscarProductos(busqueda);
        return productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerProductosConStockBajo() {
        List<Producto> productos = productoRepository.findProductosConStockBajo();
        return productos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoResponse actualizarStock(UUID productoId, Integer nuevoStock, Long usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Integer stockAnterior = producto.getStockActual();
        producto.setStockActual(nuevoStock);
        producto = productoRepository.save(producto);

        // Registrar auditoría
        auditoriaService.registrar(usuarioId, "ACTUALIZAR_STOCK", "Producto", 
                producto.getId().toString(), 
                String.format("Stock actualizado de %d a %d", stockAnterior, nuevoStock), null);

        return convertirAResponse(producto);
    }

    private ProductoResponse convertirAResponse(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setDescripcion(producto.getDescripcion());
        response.setPrecio(producto.getPrecio());
        response.setStockActual(producto.getStockActual());
        response.setStockMinimo(producto.getStockMinimo());
        response.setDisponible(producto.getStockActual() > 0);
        response.setFechaCreacion(producto.getFechaCreacion());
        response.setFechaActualizacion(producto.getFechaActualizacion());
        
        // Mapear imágenes
        if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            List<ProductoResponse.ImagenResponse> imagenes = producto.getImagenes().stream()
                    .map(img -> new ProductoResponse.ImagenResponse(
                            img.getId(),
                            img.getNombreArchivo(),
                            img.getRutaArchivo(),
                            img.getTipoMime(),
                            img.getTamañoArchivo()
                    ))
                    .collect(java.util.stream.Collectors.toList());
            response.setImagenes(imagenes);
        }
        
        return response;
    }
}
