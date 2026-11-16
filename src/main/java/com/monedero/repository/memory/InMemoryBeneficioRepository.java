package com.monedero.repository.memory;

import com.monedero.domain.model.Beneficio;
import com.monedero.domain.model.TipoBeneficio;
import com.monedero.repository.BeneficioRepository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryBeneficioRepository implements BeneficioRepository {
    private final Map<UUID, Beneficio> datos = new LinkedHashMap<>();

    public InMemoryBeneficioRepository() {
        cargarCatalogoInicial();
    }

    private void cargarCatalogoInicial() {
        guardar(new Beneficio(
                "Descuento 10% en Transferencias",
                "Reduce en 10% la comision aplicada a tu proxima transferencia.",
                100,
                TipoBeneficio.DESCUENTO_COMISION_TRANSFERENCIA,
                new BigDecimal("0.10")
        ));
        guardar(new Beneficio(
                "Mes sin Cargos por Retiros",
                "Exencion total de comisiones en retiros durante 30 dias.",
                500,
                TipoBeneficio.EXENCION_CARGOS_RETIRO,
                BigDecimal.ONE
        ));
        guardar(new Beneficio(
                "Bono de Saldo $50",
                "Acredita 50.00 unidades directamente en tu monedero principal.",
                1000,
                TipoBeneficio.BONO_SALDO,
                new BigDecimal("50.00")
        ));
    }

    @Override
    public void guardar(Beneficio beneficio) {
        if (beneficio != null) {
            datos.put(beneficio.getId(), beneficio);
        }
    }

    @Override
    public List<Beneficio> obtenerActivos() {
        return datos.values().stream()
                .filter(Beneficio::estaDisponible)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Beneficio> buscarPorId(UUID id) {
        return Optional.ofNullable(datos.get(id));
    }
}
