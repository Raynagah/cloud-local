package com.backend.notificaciones.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.notificaciones.dto.OrdenCreadaEvent;
import com.backend.notificaciones.dto.UsuarioActualizadoEvent;
import com.backend.notificaciones.dto.UsuarioRegistradoEvent;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.repository.NotificacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;

    public List<Notificacion> obtenerPorUsuario(String usuarioCorreo) {
        return notificacionRepository.findByUsuarioCorreoOrderByFechaCreacionDesc(usuarioCorreo);
    }

    @Transactional
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));
        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }

    public void eliminarNotificacion(Long id) {
        if (!notificacionRepository.existsById(id)) {
            throw new RuntimeException("La notificación con ID " + id + " no existe.");
        }
        notificacionRepository.deleteById(id);
    }

    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }

    @Transactional
    public void crearNotificacionDesdeOrden(OrdenCreadaEvent evento) {
        // 1. Validar el nombre del usuario
        String nombreUsuario = (evento.getUsuarioNombre() != null && !evento.getUsuarioNombre().isBlank()) 
                ? evento.getUsuarioNombre() 
                : "Cliente";

        // 2. Darle un formato elegante a la fecha (Ej: 07 de octubre de 2026 a las 15:30)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es", "ES"));
        String fechaFormateada = (evento.getFechaCreacion() != null) 
                ? evento.getFechaCreacion().format(formatter) 
                : LocalDateTime.now().format(formatter);

        // 3. Título del correo y la notificación
        String titulo = "¡Confirmación de Pedido #" + evento.getOrdenId() + " - En preparación!";
        
        // 4. Construir el mensaje contundente y detallado
        String mensaje = String.format(
                "Hola %s,\n\n" +
                "¡Gracias por tu compra en Pedidos360! Hemos recibido tu pago exitosamente y tu orden ya está en nuestras manos.\n\n" +
                "Aquí tienes el resumen detallado de tu transacción:\n" +
                "--------------------------------------------------\n" +
                "📦 Número de Orden: #%s\n" +
                "📅 Fecha de Compra: %s\n" +
                "💳 Total Pagado: $%s\n" +
                "🔄 Estado Actual: %s\n" +
                "--------------------------------------------------\n\n" +
                "Nuestro equipo de bodega ya se encuentra empaquetando tus productos. Te enviaremos un nuevo aviso tan pronto como el paquete sea despachado y vaya en camino a tu domicilio.\n\n" +
                "Si tienes alguna duda con tu pedido, no dudes en contactarnos.\n\n" +
                "Saludos cordiales,\n" +
                "El equipo de Pedidos360",
                nombreUsuario,
                evento.getOrdenId(),
                fechaFormateada,
                evento.getTotal(),
                (evento.getEstado() != null ? evento.getEstado() : "EN PREPARACIÓN")
        );

        // 5. Guardar la notificación en la Base de Datos
        Notificacion notificacion = Notificacion.builder()
                .ordenId(evento.getOrdenId())
                .usuarioCorreo(evento.getUsuarioCorreo())
                .titulo(titulo)
                .mensaje(mensaje)
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .build();

        notificacionRepository.save(notificacion);

        // 6. Disparar el envío de correo electrónico
        emailService.enviarCorreoNotificacion(
                notificacion.getUsuarioCorreo(),
                notificacion.getTitulo(),
                notificacion.getMensaje()
        );
    }

    @Transactional
    public void crearNotificacionBienvenida(UsuarioRegistradoEvent evento) {
        // 1. Manejo de nulos preventivo
        String nombreUsuario = (evento.nombre() != null && !evento.nombre().isBlank()) ? evento.nombre() : "Usuario";
        String tipoCuenta = (evento.tipoUsuario() != null) ? evento.tipoUsuario() : "Cliente Estándar";

        // 2. Título atractivo
        String titulo = "🎉 ¡Bienvenido a Pedidos360, " + nombreUsuario + "!";

        // 3. Mensaje estructurado
        String mensaje = String.format(
                "Hola %s,\n\n" +
                "¡Estamos emocionados de tenerte con nosotros! Tu cuenta ha sido creada de manera exitosa y ya eres oficialmente parte de la comunidad de Pedidos360.\n\n" +
                "Aquí tienes los detalles de tu registro:\n" +
                "--------------------------------------------------\n" +
                "👤 Nombre: %s\n" +
                "📧 Correo (Tu usuario): %s\n" +
                "🏷️ Tipo de Cuenta: %s\n" +
                "--------------------------------------------------\n\n" +
                "Ya puedes comenzar a explorar nuestra plataforma, descubrir nuestros productos y realizar tus pedidos con total comodidad.\n\n" +
                "Si tienes alguna pregunta, nuestro equipo de soporte está siempre disponible para ayudarte.\n\n" +
                "¡Que disfrutes la experiencia!\n\n" +
                "Saludos,\n" +
                "El equipo de Pedidos360",
                nombreUsuario,
                nombreUsuario,
                evento.correo(),
                tipoCuenta
        );

        Notificacion notificacion = Notificacion.builder()
                .usuarioCorreo(evento.correo())
                .titulo(titulo)
                .mensaje(mensaje)
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .build();

        notificacionRepository.save(notificacion);

        // Envío de correo automático
        emailService.enviarCorreoNotificacion(evento.correo(), titulo, mensaje);
    }

    @Transactional
    public void crearNotificacionPerfilActualizado(UsuarioActualizadoEvent evento) {
        // 1. Manejo de nulos para campos opcionales
        String nombreUsuario = (evento.nombre() != null && !evento.nombre().isBlank()) ? evento.nombre() : "Usuario";
        String telefono = (evento.telefono() != null && !evento.telefono().isBlank()) ? evento.telefono() : "No especificado";
        String direccion = (evento.direccion() != null && !evento.direccion().isBlank()) ? evento.direccion() : "No especificada";

        // 2. Título claro
        String titulo = "✅ Tu perfil ha sido actualizado con éxito";

        // 3. Mensaje estructurado con advertencia de seguridad
        String mensaje = String.format(
                "Hola %s,\n\n" +
                "Te confirmamos que la información de tu perfil ha sido actualizada correctamente en nuestra plataforma.\n\n" +
                "Este es el resumen de tus datos actuales:\n" +
                "--------------------------------------------------\n" +
                "👤 Nombre: %s\n" +
                "📧 Correo: %s\n" +
                "📱 Teléfono: %s\n" +
                "📍 Dirección: %s\n" +
                "--------------------------------------------------\n\n" +
                "🔒 Seguridad: Si tú no realizaste estos cambios, por favor contáctanos de inmediato para proteger tu cuenta.\n\n" +
                "Mantener tu información al día nos ayuda a brindarte un mejor servicio, especialmente al momento de despachar tus futuras compras.\n\n" +
                "Saludos,\n" +
                "El equipo de Pedidos360",
                nombreUsuario,
                nombreUsuario,
                evento.correo(),
                telefono,
                direccion
        );

        Notificacion notificacion = Notificacion.builder()
                .usuarioCorreo(evento.correo())
                .titulo(titulo)
                .mensaje(mensaje)
                .fechaCreacion(LocalDateTime.now())
                .leido(false)
                .build();

        notificacionRepository.save(notificacion);

        // Envío de correo automático
        emailService.enviarCorreoNotificacion(evento.correo(), titulo, mensaje);
    }
}