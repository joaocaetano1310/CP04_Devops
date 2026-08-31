package br.com.fiap.agrovision.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SafraResponse(
        Long id,
        Long plantacaoId,
        String tipoPlantio,
        LocalDate dataColheita,
        BigDecimal qtdColhida
) {}
