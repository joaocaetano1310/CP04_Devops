package br.com.fiap.agrovision.dto.request;

import br.com.fiap.agrovision.entity.Plantacao;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PlantacaoRequest(

        @NotNull(message = "ID do usuário é obrigatório")
        Long usuarioId,

        @NotBlank(message = "Tipo de plantio é obrigatório")
        @Size(max = 50, message = "Tipo de plantio deve ter no máximo 50 caracteres")
        String tipoPlantio,

        @NotNull(message = "Área do plantio é obrigatória")
        @Positive(message = "Área deve ser um valor positivo")
        Long areaPlantio,

        @NotNull(message = "Data do plantio é obrigatória")
        LocalDate dataPlantio,

        @NotBlank(message = "Local do plantio é obrigatório")
        @Size(max = 20, message = "Local do plantio deve ter no máximo 20 caracteres")
        String localPlantio,

        Plantacao.StatusPlantio status
) {}
