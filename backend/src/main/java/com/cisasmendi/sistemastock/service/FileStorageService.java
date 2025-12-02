package com.cisasmendi.sistemastock.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cisasmendi.sistemastock.config.FileStorageProperties;
import com.cisasmendi.sistemastock.exception.FileStorageException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getPath())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("No se pudo crear el directorio donde se almacenarán los archivos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException(
                        "El nombre del archivo contiene una secuencia de ruta no válida: " + originalFileName);
            }

            // Generate unique filename
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo almacenado: {} como {}", originalFileName, uniqueFileName);
            return uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException(
                    "No se pudo almacenar el archivo " + originalFileName + ". Inténtalo de nuevo!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("Archivo no encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Archivo no encontrado: " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Archivo eliminado: {}", fileName);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo eliminar el archivo: " + fileName, ex);
        }
    }

    public long getFileSize(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo obtener el tamaño del archivo: " + fileName, ex);
        }
    }

    // Métodos adicionales para gestión de imágenes de productos

    public String guardarArchivo(MultipartFile file, String subdirectorio) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new FileStorageException(
                        "El nombre del archivo contiene una secuencia de ruta no válida: " + originalFileName);
            }

            // Crear subdirectorio si no existe
            Path subdirPath = this.fileStorageLocation.resolve(subdirectorio);
            Files.createDirectories(subdirPath);

            // Generar nombre único
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Ruta completa con subdirectorio
            String rutaCompleta = subdirectorio + "/" + uniqueFileName;
            Path targetLocation = this.fileStorageLocation.resolve(rutaCompleta);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo almacenado: {} como {} en {}", originalFileName, uniqueFileName, subdirectorio);
            return rutaCompleta;
        } catch (IOException ex) {
            throw new FileStorageException(
                    "No se pudo almacenar el archivo " + originalFileName + ". Inténtalo de nuevo!", ex);
        }
    }

    public Path obtenerRutaArchivo(String rutaRelativa) {
        return this.fileStorageLocation.resolve(rutaRelativa).normalize();
    }

    public void eliminarArchivo(String rutaRelativa) {
        try {
            Path filePath = this.fileStorageLocation.resolve(rutaRelativa).normalize();
            Files.deleteIfExists(filePath);
            log.info("Archivo eliminado: {}", rutaRelativa);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo eliminar el archivo: " + rutaRelativa, ex);
        }
    }
}