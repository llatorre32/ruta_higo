package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.dto.request.*;
import com.cisasmendi.sistemastock.dto.response.PedidoResponse;
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
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public PedidoResponse crearPedidoDesdeCarrito(Long usuarioId, CrearPedidoRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        pedido.setDireccionEnvio(request.getDireccionEnvio());
        pedido.setTelefonoContacto(request.getTelefonoContacto());
        pedido.setNotas(request.getNotas());
        pedido.setEsVentaPresencial(false);

        // Convertir items del carrito a items del pedido
        double total = 0.0;
        for (ItemCarrito itemCarrito : carrito.getItems()) {
            Producto producto = itemCarrito.getProducto();

            // Validar stock
            if (producto.getStockActual() < itemCarrito.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Crear item del pedido
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProducto(producto);
            itemPedido.setNombreProducto(producto.getNombre());
            itemPedido.setCantidad(itemCarrito.getCantidad());
            itemPedido.setPrecioUnitario(itemCarrito.getPrecioUnitario());
            pedido.agregarItem(itemPedido);

            total += itemPedido.getSubtotal();

            // Reservar stock (descuento temporal)
            producto.setStockActual(producto.getStockActual() - itemCarrito.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        // Limpiar carrito
        itemCarritoRepository.deleteByCarritoId(carrito.getId());
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        // Enviar email de confirmación
        try {
            emailService.enviarConfirmacionPedido(usuario.getEmail(), pedido);
        } catch (Exception e) {
            // Log error pero no fallar la operación
        }

        // Registrar auditoría
        auditoriaService.registrar(usuario.getId(), "CREAR_PEDIDO", "Pedido", 
                pedido.getId().toString(), "Pedido creado desde carrito", null);

        return convertirAResponse(pedido);
    }

    @Transactional
    public PedidoResponse crearVentaPresencial(Long managerId, CrearVentaPresencialRequest request) {
        Usuario manager = usuarioRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.PAGADO); // Las ventas presenciales se pagan inmediatamente
        pedido.setNotas(request.getNotasVenta());
        pedido.setEsVentaPresencial(true);
        pedido.setFechaPago(LocalDateTime.now());

        double total = 0.0;
        for (CrearVentaPresencialRequest.ItemVentaRequest itemRequest : request.getItems()) {
            Producto producto = productoRepository.findById(itemRequest.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Validar stock
            if (producto.getStockActual() < itemRequest.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Crear item del pedido
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProducto(producto);
            itemPedido.setNombreProducto(producto.getNombre());
            itemPedido.setCantidad(itemRequest.getCantidad());
            itemPedido.setPrecioUnitario(producto.getPrecio());
            pedido.agregarItem(itemPedido);

            total += itemPedido.getSubtotal();

            // Descontar stock
            producto.setStockActual(producto.getStockActual() - itemRequest.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        // Registrar auditoría
        auditoriaService.registrar(manager.getId(), "VENTA_PRESENCIAL", "Pedido", 
                pedido.getId().toString(), "Venta presencial registrada", null);

        return convertirAResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerMisPedidos(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return pedidos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedidoPorId(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        return convertirAResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerTodosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        return pedidos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> obtenerPedidosPorEstado(EstadoPedido estado) {
        List<Pedido> pedidos = pedidoRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        return pedidos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoResponse confirmarPago(Long pedidoId, Long managerId, ConfirmarPagoRequest request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new RuntimeException("El pedido no está pendiente de pago");
        }

        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setComprobantePago(request.getComprobantePago());
        pedido.setFechaPago(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        // Enviar email de confirmación
        if (pedido.getUsuario() != null) {
            try {
                emailService.enviarConfirmacionPago(pedido.getUsuario().getEmail(), pedido);
            } catch (Exception e) {
                // Log error
            }
        }

        // Registrar auditoría
        auditoriaService.registrar(managerId, "CONFIRMAR_PAGO", "Pedido", 
                pedido.getId().toString(), "Pago confirmado", null);

        return convertirAResponse(pedido);
    }

    @Transactional
    public PedidoResponse asignarCodigoEnvio(Long pedidoId, Long managerId, AsignarCodigoEnvioRequest request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.PAGADO) {
            throw new RuntimeException("El pedido debe estar pagado para asignar código de envío");
        }

        pedido.setCodigoEnvio(request.getCodigoEnvio());
        pedido.setEstado(EstadoPedido.EN_DESPACHO);
        pedido.setFechaDespacho(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        // Enviar email con código de envío
        if (pedido.getUsuario() != null) {
            try {
                emailService.enviarCodigoEnvio(pedido.getUsuario().getEmail(), pedido);
            } catch (Exception e) {
                // Log error
            }
        }

        // Registrar auditoría
        auditoriaService.registrar(managerId, "ASIGNAR_CODIGO_ENVIO", "Pedido", 
                pedido.getId().toString(), "Código de envío: " + request.getCodigoEnvio(), null);

        return convertirAResponse(pedido);
    }

    @Transactional
    public PedidoResponse marcarComoEntregado(Long pedidoId, Long managerId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.EN_DESPACHO) {
            throw new RuntimeException("El pedido debe estar en despacho para marcarlo como entregado");
        }

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaEntrega(LocalDateTime.now());
        pedido = pedidoRepository.save(pedido);

        // Registrar auditoría
        auditoriaService.registrar(managerId, "MARCAR_ENTREGADO", "Pedido", 
                pedido.getId().toString(), "Pedido marcado como entregado", null);

        return convertirAResponse(pedido);
    }

    @Transactional
    public void cancelarPedido(Long pedidoId, Long usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para cancelar este pedido");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new RuntimeException("Solo se pueden cancelar pedidos pendientes de pago");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);

        // Devolver stock
        for (ItemPedido item : pedido.getItems()) {
            Producto producto = item.getProducto();
            producto.setStockActual(producto.getStockActual() + item.getCantidad());
            productoRepository.save(producto);
        }

        // Registrar auditoría
        auditoriaService.registrar(usuarioId, "CANCELAR_PEDIDO", "Pedido", 
                pedido.getId().toString(), "Pedido cancelado por usuario", null);
    }

    @Transactional
    public void limpiarPedidosExpirados() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Pedido> pedidosExpirados = pedidoRepository
                .findPedidosConReservaExpirada(ahora, EstadoPedido.PENDIENTE_PAGO);

        for (Pedido pedido : pedidosExpirados) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);

            // Devolver stock
            for (ItemPedido item : pedido.getItems()) {
                Producto producto = item.getProducto();
                producto.setStockActual(producto.getStockActual() + item.getCantidad());
                productoRepository.save(producto);
            }
        }
    }

    private PedidoResponse convertirAResponse(Pedido pedido) {
        PedidoResponse response = new PedidoResponse();
        response.setId(pedido.getId());
        
        if (pedido.getUsuario() != null) {
            response.setUsuarioId(pedido.getUsuario().getId());
            response.setUsuarioNombre(pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido());
        }
        
        response.setTotal(pedido.getTotal());
        response.setEstado(pedido.getEstado().name());
        response.setFechaCreacion(pedido.getFechaCreacion());
        response.setFechaPago(pedido.getFechaPago());
        response.setFechaDespacho(pedido.getFechaDespacho());
        response.setFechaEntrega(pedido.getFechaEntrega());
        response.setFechaExpiracionReserva(pedido.getFechaExpiracionReserva());
        response.setCodigoEnvio(pedido.getCodigoEnvio());
        response.setComprobantePago(pedido.getComprobantePago());
        response.setDireccionEnvio(pedido.getDireccionEnvio());
        response.setTelefonoContacto(pedido.getTelefonoContacto());
        response.setNotas(pedido.getNotas());
        response.setEsVentaPresencial(pedido.getEsVentaPresencial());

        List<PedidoResponse.ItemPedidoResponse> items = pedido.getItems().stream()
                .map(item -> {
                    PedidoResponse.ItemPedidoResponse itemResponse = new PedidoResponse.ItemPedidoResponse();
                    itemResponse.setId(item.getId());
                    itemResponse.setProductoId(item.getProducto().getId());
                    itemResponse.setNombreProducto(item.getNombreProducto());
                    itemResponse.setCantidad(item.getCantidad());
                    itemResponse.setPrecioUnitario(item.getPrecioUnitario());
                    itemResponse.setSubtotal(item.getSubtotal());
                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(items);

        return response;
    }
}
