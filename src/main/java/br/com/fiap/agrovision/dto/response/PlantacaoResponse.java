package br.com.fiap.agrovision.dto.response;

import br.com.fiap.agrovision.entity.Plantacao;

import java.time.LocalDate;

public record PlantacaoResponse(
        Long id,
        Long usuarioId,
        String nomeUsuario,
        String nomeFazenda,
        String tipoPlantio,
        Long areaPlantio,
        LocalDate dataPlantio,
        String localPlantio,
        Plantacao.StatusPlantio status
) {}
