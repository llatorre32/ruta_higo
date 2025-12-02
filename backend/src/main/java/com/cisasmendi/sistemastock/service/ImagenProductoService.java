package com.cisasmendi.sistemastock.service;

import com.cisasmendi.sistemastock.model.ImagenProducto;
import com.cisasmendi.sistemastock.model.Producto;
import com.cisasmendi.sistemastock.repository.ImagenProductoRepository;
import com.cisasmendi.sistemastock.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImagenProductoService {

    private final ImagenProductoRepository imagenProductoRepository;
    private final ProductoRepository productoRepository;
    private final FileStorageService fileStorageService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Map<String, Object> guardarImagen(UUID productoId, MultipartFile file, Long usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Validar archivo
        validarImagen(file);

        // Guardar archivo físicamente
        String nombreArchivo = fileStorageService.guardarArchivo(file, "productos/" + productoId);

        // Crear registro en BD
        ImagenProducto imagen = new ImagenProducto();
        imagen.setProducto(producto);
        imagen.setNombreArchivo(file.getOriginalFilename());
        imagen.setRutaArchivo(nombreArchivo);
        imagen.setTipoMime(file.getContentType());
        imagen.setTamañoArchivo(file.getSize());

        imagen = imagenProductoRepository.save(imagen);

        // Auditoría
        auditoriaService.registrar(usuarioId, "AGREGAR_IMAGEN_PRODUCTO", "ImagenProducto",
                imagen.getId().toString(), "Imagen agregada al producto: " + producto.getNombre(), null);

        Map<String, Object> response = new HashMap<>();
        response.put("id", imagen.getId());
        response.put("nombreArchivo", imagen.getNombreArchivo());
        response.put("rutaArchivo", imagen.getRutaArchivo());
        response.put("mensaje", "Imagen subida exitosamente");

        return response;
    }

    @Transactional
    public Map<String, Object> guardarMultiplesImagenes(UUID productoId, List<MultipartFile> files, Long usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        List<Map<String, Object>> imagenesGuardadas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                validarImagen(file);
                String nombreArchivo = fileStorageService.guardarArchivo(file, "productos/" + productoId);

                ImagenProducto imagen = new ImagenProducto();
                imagen.setProducto(producto);
                imagen.setNombreArchivo(file.getOriginalFilename());
                imagen.setRutaArchivo(nombreArchivo);
                imagen.setTipoMime(file.getContentType());
                imagen.setTamañoArchivo(file.getSize());

                imagen = imagenProductoRepository.save(imagen);

                Map<String, Object> imagenInfo = new HashMap<>();
                imagenInfo.put("id", imagen.getId());
                imagenInfo.put("nombreArchivo", imagen.getNombreArchivo());
                imagenesGuardadas.add(imagenInfo);

            } catch (Exception e) {
                errores.add("Error con archivo " + file.getOriginalFilename() + ": " + e.getMessage());
                log.error("Error guardando imagen: {}", file.getOriginalFilename(), e);
            }
        }

        // Auditoría
        auditoriaService.registrar(usuarioId, "AGREGAR_IMAGENES_PRODUCTO", "ImagenProducto",
                productoId.toString(), imagenesGuardadas.size() + " imágenes agregadas", null);

        Map<String, Object> response = new HashMap<>();
        response.put("imagenesGuardadas", imagenesGuardadas);
        response.put("cantidadExitosas", imagenesGuardadas.size());
        response.put("cantidadErrores", errores.size());
        if (!errores.isEmpty()) {
            response.put("errores", errores);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Resource cargarImagen(UUID imagenId) {
        ImagenProducto imagen = imagenProductoRepository.findById(imagenId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));

        try {
            Path rutaArchivo = fileStorageService.obtenerRutaArchivo(imagen.getRutaArchivo());
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se pudo leer el archivo: " + imagen.getNombreArchivo());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al cargar imagen: " + imagen.getNombreArchivo(), e);
        }
    }

    @Transactional(readOnly = true)
    public String obtenerTipoMime(UUID imagenId) {
        ImagenProducto imagen = imagenProductoRepository.findById(imagenId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));
        return imagen.getTipoMime();
    }

    @Transactional
    public void eliminarImagen(UUID imagenId, Long usuarioId) {
        ImagenProducto imagen = imagenProductoRepository.findById(imagenId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));

        String nombreProducto = imagen.getProducto().getNombre();

        // Eliminar archivo físico
        try {
            fileStorageService.eliminarArchivo(imagen.getRutaArchivo());
        } catch (Exception e) {
            log.error("Error eliminando archivo físico: {}", imagen.getRutaArchivo(), e);
        }

        // Eliminar registro
        imagenProductoRepository.delete(imagen);

        // Auditoría
        auditoriaService.registrar(usuarioId, "ELIMINAR_IMAGEN_PRODUCTO", "ImagenProducto",
                imagenId.toString(), "Imagen eliminada del producto: " + nombreProducto, null);
    }

    @Transactional
    public void eliminarTodasLasImagenes(UUID productoId, Long usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoId(productoId);

        for (ImagenProducto imagen : imagenes) {
            try {
                fileStorageService.eliminarArchivo(imagen.getRutaArchivo());
            } catch (Exception e) {
                log.error("Error eliminando archivo físico: {}", imagen.getRutaArchivo(), e);
            }
        }

        imagenProductoRepository.deleteByProductoId(productoId);

        // Auditoría
        auditoriaService.registrar(usuarioId, "ELIMINAR_TODAS_IMAGENES_PRODUCTO", "Producto",
                productoId.toString(), "Todas las imágenes eliminadas del producto: " + producto.getNombre(), null);
    }

    private void validarImagen(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        // Validar tamaño (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("El archivo no puede superar los 5MB");
        }

        // Validar extensión
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            List<String> extensionesPermitidas = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
            if (!extensionesPermitidas.contains(extension)) {
                throw new RuntimeException("Extensión de archivo no permitida. Use: jpg, jpeg, png, gif, webp");
            }
        }
    }
}
