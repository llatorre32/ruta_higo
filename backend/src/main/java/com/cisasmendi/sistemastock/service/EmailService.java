package com.cisasmendi.sistemastock.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.cisasmendi.sistemastock.model.Pedido;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@sistemastock.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void enviarEmailVerificacion(String toEmail, String nombreUsuario, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Confirma tu cuenta - Sistema de Stock");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                    "Por favor confirma tu cuenta haciendo clic en el siguiente enlace:\n" +
                    "%s/verificar-email?token=%s\n\n" +
                    "Este enlace expirará en 24 horas.\n\n" +
                    "Si no creaste esta cuenta, puedes ignorar este email.\n\n" +
                    "Saludos,\n" +
                    "Equipo Sistema de Stock",
                    nombreUsuario, frontendUrl, token));

            mailSender.send(message);
            log.info("Email de verificación enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando email de verificación a: {}", toEmail, e);
            throw new RuntimeException("Error enviando email de verificación");
        }
    }

    public void enviarEmailRecuperacion(String toEmail, String nombreUsuario, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperación de contraseña - Sistema de Stock");
            message.setText(String.format(
                    "Hola %s,\n\n" +
                    "Recibimos una solicitud para restablecer tu contraseña.\n" +
                    "Haz clic en el siguiente enlace para crear una nueva contraseña:\n" +
                    "%s/restablecer-password?token=%s\n\n" +
                    "Este enlace expirará en 1 hora.\n\n" +
                    "Si no solicitaste restablecer tu contraseña, puedes ignorar este email.\n\n" +
                    "Saludos,\n" +
                    "Equipo Sistema de Stock",
                    nombreUsuario, frontendUrl, token));

            mailSender.send(message);
            log.info("Email de recuperación enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando email de recuperación a: {}", toEmail, e);
            throw new RuntimeException("Error enviando email de recuperación");
        }
    }

    public void enviarConfirmacionPedido(String toEmail, Pedido pedido) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Pedido Recibido #" + pedido.getId() + " - Sistema de Stock");
            message.setText(String.format(
                    "Tu pedido ha sido recibido exitosamente.\n\n" +
                    "Número de pedido: #%d\n" +
                    "Total: $%.2f\n" +
                    "Estado: Pendiente de pago\n\n" +
                    "Para completar tu pedido, realiza la transferencia bancaria y envía el comprobante.\n" +
                    "El pedido estará reservado por 3 días. Si no se confirma el pago, será cancelado automáticamente.\n\n" +
                    "Ver detalles: %s/pedidos/%d\n\n" +
                    "Gracias por tu compra,\n" +
                    "Equipo Sistema de Stock",
                    pedido.getId(), pedido.getTotal(), frontendUrl, pedido.getId()));

            mailSender.send(message);
            log.info("Email de confirmación de pedido enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando email de confirmación de pedido a: {}", toEmail, e);
        }
    }

    public void enviarConfirmacionPago(String toEmail, Pedido pedido) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Pago Confirmado #" + pedido.getId() + " - Sistema de Stock");
            message.setText(String.format(
                    "Tu pago ha sido confirmado exitosamente.\n\n" +
                    "Número de pedido: #%d\n" +
                    "Total: $%.2f\n" +
                    "Estado: Pagado\n\n" +
                    "Tu pedido está siendo preparado para envío. Te notificaremos cuando sea despachado.\n\n" +
                    "Ver detalles: %s/pedidos/%d\n\n" +
                    "Gracias por tu compra,\n" +
                    "Equipo Sistema de Stock",
                    pedido.getId(), pedido.getTotal(), frontendUrl, pedido.getId()));

            mailSender.send(message);
            log.info("Email de confirmación de pago enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando email de confirmación de pago a: {}", toEmail, e);
        }
    }

    public void enviarCodigoEnvio(String toEmail, Pedido pedido) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Pedido Despachado #" + pedido.getId() + " - Sistema de Stock");
            message.setText(String.format(
                    "Tu pedido ha sido despachado.\n\n" +
                    "Número de pedido: #%d\n" +
                    "Código de envío: %s\n" +
                    "Total: $%.2f\n\n" +
                    "Puedes hacer seguimiento de tu envío con el código proporcionado.\n\n" +
                    "Ver detalles: %s/pedidos/%d\n\n" +
                    "Gracias por tu compra,\n" +
                    "Equipo Sistema de Stock",
                    pedido.getId(), pedido.getCodigoEnvio(), pedido.getTotal(), frontendUrl, pedido.getId()));

            mailSender.send(message);
            log.info("Email de código de envío enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("Error enviando email de código de envío a: {}", toEmail, e);
        }
    }
}