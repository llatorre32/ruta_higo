package com.cisasmendi.sistemastock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {
    private String path = "./files";
}