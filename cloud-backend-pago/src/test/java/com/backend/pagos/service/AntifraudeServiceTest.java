package com.backend.pagos.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.backend.pagos.dto.EvaluacionFraude;
import com.backend.pagos.model.AlertaFraude;
import com.backend.pagos.model.EstadoPagoCliente;
import com.backend.pagos.repository.AlertaFraudeRepositorio;
import com.backend.pagos.repository.EstadoPagoClienteRepositorio;

class AntifraudeServiceTest {

    /** Reloj controlable para simular el paso de los segundos. */
    static class RelojMutable extends Clock {
        private Instant ahora = Instant.parse("2026-01-01T12:00:00Z");

        void avanzar(long segundos) {
            ahora = ahora.plusSeconds(segundos);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    private EstadoPagoClienteRepositorio estadoRepo;
    private AlertaFraudeRepositorio alertaRepo;
    private FraudeEventPublisher publisher;
    private RelojMutable reloj;
    private AntifraudeService servicio;
    private final Map<String, EstadoPagoCliente> almacen = new HashMap<>();

    @BeforeEach
    void preparar() {
        almacen.clear();
        estadoRepo = mock(EstadoPagoClienteRepositorio.class);
        alertaRepo = mock(AlertaFraudeRepositorio.class);
        publisher = mock(FraudeEventPublisher.class);
        reloj = new RelojMutable();

        when(estadoRepo.findById(any(String.class)))
                .thenAnswer(inv -> Optional.ofNullable(almacen.get((String) inv.getArgument(0))));
        when(estadoRepo.save(any(EstadoPagoCliente.class))).thenAnswer(inv -> {
            EstadoPagoCliente e = inv.getArgument(0);
            almacen.put(e.getIdCliente(), e);
            return e;
        });
        when(alertaRepo.save(any(AlertaFraude.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio = new AntifraudeService(estadoRepo, alertaRepo, publisher, reloj);
    }

    @Test
    void tresUsosEnDiezSegundosSePermiten() {
        String clave = AntifraudeService.construirClave("4242", "cliente-1");
        for (int i = 0; i < 3; i++) {
            EvaluacionFraude r = servicio.evaluarIntento(clave, "cliente-1");
            assertFalse(r.bloqueado(), "El intento " + (i + 1) + " debe permitirse");
            reloj.avanzar(1);
        }
        verify(publisher, times(0)).publicarAlerta(any());
    }

    @Test
    void elCuartoUsoEnDiezSegundosSeBloqueaYGeneraAlerta() {
        String clave = AntifraudeService.construirClave("4242", "cliente-1");
        for (int i = 0; i < 3; i++) {
            servicio.evaluarIntento(clave, "cliente-1");
            reloj.avanzar(1);
        }
        EvaluacionFraude cuarto = servicio.evaluarIntento(clave, "cliente-1");

        assertTrue(cuarto.bloqueado());
        assertTrue(cuarto.nuevaAlerta());
        verify(alertaRepo, times(1)).save(any(AlertaFraude.class));
        verify(publisher, times(1)).publicarAlerta(any(AlertaFraude.class));
    }

    @Test
    void mientrasDureElBloqueoSeSiguenRechazandoSinNuevasAlertas() {
        String clave = AntifraudeService.construirClave("4242", "cliente-1");
        for (int i = 0; i < 4; i++) {
            servicio.evaluarIntento(clave, "cliente-1");
        }
        reloj.avanzar(60);
        EvaluacionFraude r = servicio.evaluarIntento(clave, "cliente-1");

        assertTrue(r.bloqueado());
        assertFalse(r.nuevaAlerta());
        verify(alertaRepo, times(1)).save(any(AlertaFraude.class));
    }

    @Test
    void intentosEspaciadosMasDeDiezSegundosNuncaBloquean() {
        String clave = AntifraudeService.construirClave("4242", "cliente-1");
        for (int i = 0; i < 8; i++) {
            EvaluacionFraude r = servicio.evaluarIntento(clave, "cliente-1");
            assertFalse(r.bloqueado());
            reloj.avanzar(11);
        }
    }

    @Test
    void elBloqueoExpiraDespuesDeTreintaMinutos() {
        String clave = AntifraudeService.construirClave("4242", "cliente-1");
        for (int i = 0; i < 4; i++) {
            servicio.evaluarIntento(clave, "cliente-1");
        }
        reloj.avanzar(31 * 60);

        assertFalse(servicio.evaluarIntento(clave, "cliente-1").bloqueado());
    }

    @Test
    void tarjetasDistintasSeEvaluanPorSeparado() {
        String a = AntifraudeService.construirClave("1111", "cliente-1");
        String b = AntifraudeService.construirClave("2222", "cliente-1");
        for (int i = 0; i < 3; i++) {
            servicio.evaluarIntento(a, "cliente-1");
            servicio.evaluarIntento(b, "cliente-1");
        }
        assertFalse(servicio.evaluarIntento(b.replace("2222", "3333"), "cliente-1").bloqueado());
    }
}
