package br.com.fiap.agrovision.controller;

import br.com.fiap.agrovision.dto.response.LogErroResponse;
import br.com.fiap.agrovision.service.LogErroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Logs de Erro", description = "Auditoria de erros de procedures Oracle (TB_LOG_ERRO_GS) — somente leitura")
@SecurityRequirement(name = "bearerAuth")
public class LogErroController {

    private final LogErroService service;

    @GetMapping
    @Operation(
            summary = "Listar todos os logs de erro",
            description = "Registros gravados pelas procedures Oracle via EXCEPTION WHEN OTHERS."
    )
    public ResponseEntity<CollectionModel<EntityModel<LogErroResponse>>> listarTodos() {
        List<EntityModel<LogErroResponse>> models = service.listarTodos().stream()
                .map(this::toModel).toList();
        Link selfLink = linkTo(methodOn(LogErroController.class).listarTodos()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(models, selfLink));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar log por ID")
    public ResponseEntity<EntityModel<LogErroResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(service.buscarPorId(id)));
    }

    @GetMapping("/usuario/{nomeUser}")
    @Operation(summary = "Listar logs por nome de usuário Oracle")
    public ResponseEntity<List<LogErroResponse>> listarPorUsuario(@PathVariable String nomeUser) {
        return ResponseEntity.ok(service.listarPorUsuario(nomeUser));
    }

    @GetMapping("/procedure/{nomeProcedure}")
    @Operation(summary = "Listar logs por nome de procedure")
    public ResponseEntity<List<LogErroResponse>> listarPorProcedure(@PathVariable String nomeProcedure) {
        return ResponseEntity.ok(service.listarPorProcedure(nomeProcedure));
    }

    private EntityModel<LogErroResponse> toModel(LogErroResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(LogErroController.class).buscarPorId(response.id())).withSelfRel(),
                linkTo(methodOn(LogErroController.class).listarTodos()).withRel("logs")
        );
    }
}
