package com.cisasmendi.sistemastock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class Main {	
   public static void main(String[] args) {
	   // Cargar variables del archivo .env
	   Dotenv dotenv = Dotenv.configure()
		   .ignoreIfMissing() // evita el crash si el archivo no existe
		   .load();

	   // Cargar todas las variables del .env como propiedades del sistema
	   dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

	   SpringApplication.run(Main.class, args);
   }
}