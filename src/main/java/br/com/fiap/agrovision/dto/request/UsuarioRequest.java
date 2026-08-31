package br.com.fiap.agrovision.dto.request;

import jakarta.validation.constraints.*;

public record UsuarioRequest(

		@NotNull(message = "CPF é obrigatório")
		Long cpf,
		
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres")
        String nome,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha,

        @NotBlank(message = "Nome da fazenda é obrigatório")
        @Size(max = 60, message = "Nome da fazenda deve ter no máximo 60 caracteres")
        String nomeFazenda
) {}
