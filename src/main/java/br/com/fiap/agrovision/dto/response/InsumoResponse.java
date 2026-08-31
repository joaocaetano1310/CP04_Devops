package br.com.fiap.agrovision.dto.response;

import java.math.BigDecimal;

public record InsumoResponse(
        Long id,
        Long plantacaoId,
        String tipoPlantio,
        String nomeInsumo,
        BigDecimal qtdEstoque
) {}
