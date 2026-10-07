package com.backend.notificaciones.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@tuapp.com}")
    private String remitente;

    public void enviarCorreoNotificacion(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(remitente);
            email.setTo(destinatario);
            email.setSubject(asunto);
            email.setText(mensaje);

            mailSender.send(email);
            log.info("Correo enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar el correo a {}: {}", destinatario, e.getMessage());
        }
    }
}