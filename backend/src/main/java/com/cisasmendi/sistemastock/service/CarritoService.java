package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.dto.request.AgregarItemCarritoRequest;
import com.cisasmendi.sistemastock.dto.request.ActualizarItemCarritoRequest;
import com.cisasmendi.sistemastock.dto.response.CarritoResponse;
import com.cisasmendi.sistemastock.model.*;
import com.cisasmendi.sistemastock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CarritoResponse obtenerCarrito(Long usuarioId) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        return convertirAResponse(carrito);
    }

    @Transactional
    public CarritoResponse agregarItem(Long usuarioId, AgregarItemCarritoRequest request) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Validar stock disponible
        if (producto.getStockActual() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente");
        }

        // Verificar si ya existe el item en el carrito
        ItemCarrito itemExistente = itemCarritoRepository
                .findByCarritoIdAndProductoId(carrito.getId(), producto.getId())
                .orElse(null);

        if (itemExistente != null) {
            // Actualizar cantidad
            int nuevaCantidad = itemExistente.getCantidad() + request.getCantidad();
            if (producto.getStockActual() < nuevaCantidad) {
                throw new RuntimeException("Stock insuficiente para la cantidad solicitada");
            }
            itemExistente.setCantidad(nuevaCantidad);
            itemExistente.setFechaExpiracionReserva(LocalDateTime.now().plusDays(3));
            itemCarritoRepository.save(itemExistente);
        } else {
            // Crear nuevo item
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(request.getCantidad());
            nuevoItem.setPrecioUnitario(producto.getPrecio());
            carrito.agregarItem(nuevoItem);
            itemCarritoRepository.save(nuevoItem);
        }

        carritoRepository.save(carrito);
        return convertirAResponse(carrito);
    }

    @Transactional
    public CarritoResponse actualizarItem(Long usuarioId, Long itemId, ActualizarItemCarritoRequest request) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new RuntimeException("El item no pertenece al carrito del usuario");
        }

        // Validar stock disponible
        Producto producto = item.getProducto();
        if (producto.getStockActual() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente");
        }

        item.setCantidad(request.getCantidad());
        item.setFechaExpiracionReserva(LocalDateTime.now().plusDays(3));
        itemCarritoRepository.save(item);

        carrito.setFechaActualizacion(LocalDateTime.now());
        carritoRepository.save(carrito);

        return convertirAResponse(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(Long usuarioId, Long itemId) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new RuntimeException("El item no pertenece al carrito del usuario");
        }

        carrito.removerItem(item);
        itemCarritoRepository.delete(item);
        carritoRepository.save(carrito);

        return convertirAResponse(carrito);
    }

    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        itemCarritoRepository.deleteByCarritoId(carrito.getId());
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    @Transactional
    public void limpiarItemsExpirados() {
        LocalDateTime ahora = LocalDateTime.now();
        List<ItemCarrito> itemsExpirados = itemCarritoRepository.findItemsConReservaExpirada(ahora);
        itemCarritoRepository.deleteAll(itemsExpirados);
    }

    private Carrito obtenerOCrearCarrito(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return carritoRepository.findByUsuario(usuario)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    private CarritoResponse convertirAResponse(Carrito carrito) {
        CarritoResponse response = new CarritoResponse();
        response.setId(carrito.getId());
        response.setUsuarioId(carrito.getUsuario().getId());
        response.setFechaActualizacion(carrito.getFechaActualizacion());

        List<CarritoResponse.ItemCarritoResponse> items = carrito.getItems().stream()
                .map(item -> {
                    CarritoResponse.ItemCarritoResponse itemResponse = new CarritoResponse.ItemCarritoResponse();
                    itemResponse.setId(item.getId());
                    itemResponse.setProductoId(item.getProducto().getId());
                    itemResponse.setNombreProducto(item.getProducto().getNombre());
                    itemResponse.setCantidad(item.getCantidad());
                    itemResponse.setPrecioUnitario(item.getPrecioUnitario());
                    itemResponse.setSubtotal(item.getSubtotal());
                    itemResponse.setFechaExpiracionReserva(item.getFechaExpiracionReserva());
                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(items);
        response.setTotal(carrito.getTotal());

        return response;
    }
}
