package br.com.fiap.agrovision.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record InsumoRequest(

        @NotNull(message = "ID da plantação é obrigatório")
        Long plantacaoId,

        @NotBlank(message = "Nome do insumo é obrigatório")
        @Size(max = 50, message = "Nome do insumo deve ter no máximo 50 caracteres")
        String nomeInsumo,

        @NotNull(message = "Quantidade em estoque é obrigatória")
        @PositiveOrZero(message = "Estoque não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Quantidade: até 8 dígitos inteiros e 2 decimais")
        BigDecimal qtdEstoque
) {}
