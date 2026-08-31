package br.com.fiap.agrovision.dto.response;

import java.time.LocalDate;

public record LogErroResponse(
        Long id,
        String nomeProcedure,
        String nomeUser,
        LocalDate dataErro,
        Integer codigoErro,
        String mensagemErro
) {}
