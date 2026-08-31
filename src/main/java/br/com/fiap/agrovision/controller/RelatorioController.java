package br.com.fiap.agrovision.controller;

import br.com.fiap.agrovision.dto.request.RelatorioRequest;
import br.com.fiap.agrovision.dto.response.RelatorioResponse;
import br.com.fiap.agrovision.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Geração e consulta de relatórios (TB_RELATORIO_GS)")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private final RelatorioService service;

    @PostMapping
    @Operation(summary = "Criar relatório")
    public ResponseEntity<EntityModel<RelatorioResponse>> criar(@Valid @RequestBody RelatorioRequest request) {
        RelatorioResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(toModel(response));
    }

    @GetMapping
    @Operation(summary = "Listar todos os relatórios")
    public ResponseEntity<CollectionModel<EntityModel<RelatorioResponse>>> listarTodos() {
        List<EntityModel<RelatorioResponse>> models = service.listarTodos().stream()
                .map(this::toModel).toList();
        Link selfLink = linkTo(methodOn(RelatorioController.class).listarTodos()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(models, selfLink));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar relatório por ID")
    public ResponseEntity<EntityModel<RelatorioResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar relatório")
    public ResponseEntity<EntityModel<RelatorioResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody RelatorioRequest request) {
        return ResponseEntity.ok(toModel(service.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar relatório")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<RelatorioResponse> toModel(RelatorioResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(RelatorioController.class).buscarPorId(response.id())).withSelfRel(),
                linkTo(methodOn(RelatorioController.class).listarTodos()).withRel("relatorios")
        );
    }
}
