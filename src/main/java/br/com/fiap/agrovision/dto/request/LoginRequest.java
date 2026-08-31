package br.com.fiap.agrovision.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(

		@NotNull(message = "CPF é obrigatório")
		Long cpf,
		
        @NotBlank(message = "Senha é obrigatória")
        String senha
) {}
