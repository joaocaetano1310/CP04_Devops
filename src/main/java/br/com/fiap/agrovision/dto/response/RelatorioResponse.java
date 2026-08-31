package br.com.fiap.agrovision.dto.response;

import java.time.LocalDate;

public record RelatorioResponse(
        Long id,
        String titulo,
        String conteudo,
        LocalDate dataGeracao
) {}
