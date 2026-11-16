package com.monedero.app;

import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Frecuencia;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.repository.BeneficioRepository;
import com.monedero.repository.ClienteRepository;
import com.monedero.repository.MonederoRepository;
import com.monedero.repository.ProgramadaRepository;
import com.monedero.repository.TransaccionRepository;
import com.monedero.repository.memory.InMemoryBeneficioRepository;
import com.monedero.repository.memory.InMemoryClienteRepository;
import com.monedero.repository.memory.InMemoryMonederoRepository;
import com.monedero.repository.memory.InMemoryProgramadaRepository;
import com.monedero.repository.memory.InMemoryTransaccionRepository;
import com.monedero.service.AnalisisGastosService;
import com.monedero.service.BeneficioService;
import com.monedero.service.ClienteService;
import com.monedero.service.DeteccionAtipicasService;
import com.monedero.service.MonederoService;
import com.monedero.service.NotificacionService;
import com.monedero.service.ProgramacionService;
import com.monedero.service.PuntosService;
import com.monedero.service.TareaService;
import com.monedero.service.TransaccionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AppContext {
    private static AppContext instancia;

    private final ClienteService clienteService;
    private final MonederoService monederoService;
    private final NotificacionService notificacionService;
    private final PuntosService puntosService;
    private final BeneficioService beneficioService;
    private final DeteccionAtipicasService deteccionAtipicasService;
    private final TransaccionService transaccionService;
    private final ProgramacionService programacionService;
    private final AnalisisGastosService analisisGastosService;
    private final TareaService tareaService;

    private Cliente clienteActivo;
    private Runnable onActualizar;

    private AppContext() {
        ClienteRepository clienteRepo = new InMemoryClienteRepository();
        MonederoRepository monederoRepo = new InMemoryMonederoRepository();
        TransaccionRepository transaccionRepo = new InMemoryTransaccionRepository();
        ProgramadaRepository programadaRepo = new InMemoryProgramadaRepository();
        BeneficioRepository beneficioRepo = new InMemoryBeneficioRepository();

        this.clienteService = new ClienteService(clienteRepo);
        this.monederoService = new MonederoService(monederoRepo, clienteService);
        this.notificacionService = new NotificacionService(clienteRepo);
        this.puntosService = new PuntosService(clienteRepo);
        this.beneficioService = new BeneficioService(beneficioRepo, clienteRepo, monederoService);
        this.deteccionAtipicasService = new DeteccionAtipicasService(transaccionRepo, notificacionService);
        this.transaccionService = new TransaccionService(
                transaccionRepo, monederoService, puntosService, deteccionAtipicasService, notificacionService
        );
        this.programacionService = new ProgramacionService(programadaRepo, transaccionService, notificacionService);
        this.analisisGastosService = new AnalisisGastosService(transaccionRepo);
        this.tareaService = new TareaService();

        cargarDatosEjemplo();
    }

    public static synchronized AppContext getInstancia() {
        if (instancia == null) {
            instancia = new AppContext();
        }
        return instancia;
    }

    private void cargarDatosEjemplo() {
        Cliente ana = clienteService.registrarCliente("Ana Martinez", "ana.martinez@correo.com", "3001234567", "1234");
        Cliente carlos = clienteService.registrarCliente("Carlos Gomez", "carlos.gomez@correo.com", "3109876543", "5678");

        Monedero ahorrosAna = monederoService.crearMonedero(ana.getId(), "Ahorros", new BigDecimal("1200.00"));
        Monedero diariosAna = monederoService.crearMonedero(ana.getId(), "Gastos Diarios", new BigDecimal("450.00"));
        Monedero principalCarlos = monederoService.crearMonedero(carlos.getId(), "Cuenta Principal", new BigDecimal("850.00"));

        transaccionService.depositar(ahorrosAna.getId(), new BigDecimal("300.00"), "Nomina quincenal");
        transaccionService.retirar(diariosAna.getId(), new BigDecimal("120.00"), "Mercado semanal");
        transaccionService.transferir(ahorrosAna.getId(), principalCarlos.getId(), new BigDecimal("150.00"), "Pago compartido");

        programacionService.programar(
                TipoTransaccion.DEPOSITO,
                new BigDecimal("200.00"),
                LocalDateTime.now().plusDays(2),
                Frecuencia.MENSUAL,
                null,
                ahorrosAna,
                "Ahorro automatico mensual"
        );

        tareaService.crear(ana.getId(), "Revisar presupuesto mensual", "Validar gastos en supermercado", LocalDate.now().plusDays(3));
        tareaService.crear(ana.getId(), "Verificar puntos para bono", "Revisar si alcanzo los 1000 puntos", LocalDate.now().plusDays(7));
        tareaService.crear(carlos.getId(), "Programar ahorro semanal", "Separar cuota de ahorro", LocalDate.now().plusDays(5));

        this.clienteActivo = null;
    }

    public ClienteService getClienteService() {
        return clienteService;
    }

    public MonederoService getMonederoService() {
        return monederoService;
    }

    public NotificacionService getNotificacionService() {
        return notificacionService;
    }

    public PuntosService getPuntosService() {
        return puntosService;
    }

    public BeneficioService getBeneficioService() {
        return beneficioService;
    }

    public DeteccionAtipicasService getDeteccionAtipicasService() {
        return deteccionAtipicasService;
    }

    public TransaccionService getTransaccionService() {
        return transaccionService;
    }

    public ProgramacionService getProgramacionService() {
        return programacionService;
    }

    public AnalisisGastosService getAnalisisGastosService() {
        return analisisGastosService;
    }

    public TareaService getTareaService() {
        return tareaService;
    }

    public Cliente getClienteActivo() {
        return clienteActivo;
    }

    public void setClienteActivo(Cliente clienteActivo) {
        this.clienteActivo = clienteActivo;
        notificarActualizacion();
    }

    public void cerrarSesion() {
        this.clienteActivo = null;
        this.onActualizar = null;
    }

    public void setOnActualizar(Runnable onActualizar) {
        this.onActualizar = onActualizar;
    }

    public void notificarActualizacion() {
        if (onActualizar != null) {
            onActualizar.run();
        }
    }
}
