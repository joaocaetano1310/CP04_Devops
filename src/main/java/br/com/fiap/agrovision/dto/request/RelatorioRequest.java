package br.com.fiap.agrovision.dto.request;

import jakarta.validation.constraints.*;

public record RelatorioRequest(

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 100, message = "Título deve ter no máximo 100 caracteres")
        String titulo,

        @NotBlank(message = "Conteúdo é obrigatório")
        @Size(max = 1000, message = "Conteúdo deve ter no máximo 1000 caracteres")
        String conteudo
) {}
