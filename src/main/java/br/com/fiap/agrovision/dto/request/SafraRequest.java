package br.com.fiap.agrovision.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SafraRequest(

        @NotNull(message = "ID da plantação é obrigatório")
        Long plantacaoId,

        @NotNull(message = "Data de colheita é obrigatória")
        LocalDate dataColheita,

        @NotNull(message = "Quantidade colhida é obrigatória")
        @Positive(message = "Quantidade deve ser positiva")
        @Digits(integer = 13, fraction = 2, message = "Quantidade: até 13 dígitos inteiros e 2 decimais")
        BigDecimal qtdColhida
) {}
