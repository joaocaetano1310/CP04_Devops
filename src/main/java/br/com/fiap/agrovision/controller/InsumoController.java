package br.com.fiap.agrovision.controller;

import br.com.fiap.agrovision.dto.request.InsumoRequest;
import br.com.fiap.agrovision.dto.response.InsumoResponse;
import br.com.fiap.agrovision.service.InsumoService;
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
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
@Tag(name = "Insumos", description = "Controle de estoque de insumos por plantação (TB_INSUMO_GS)")
@SecurityRequirement(name = "bearerAuth")
public class InsumoController {

    private final InsumoService service;

    @PostMapping
    @Operation(summary = "Cadastrar insumo")
    public ResponseEntity<EntityModel<InsumoResponse>> criar(@Valid @RequestBody InsumoRequest request) {
        InsumoResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(toModel(response));
    }

    @GetMapping
    @Operation(summary = "Listar todos os insumos")
    public ResponseEntity<CollectionModel<EntityModel<InsumoResponse>>> listarTodos() {
        List<EntityModel<InsumoResponse>> models = service.listarTodos().stream()
                .map(this::toModel).toList();
        Link selfLink = linkTo(methodOn(InsumoController.class).listarTodos()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(models, selfLink));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar insumo por ID")
    public ResponseEntity<EntityModel<InsumoResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(service.buscarPorId(id)));
    }

    @GetMapping("/plantacao/{plantacaoId}")
    @Operation(summary = "Listar insumos por plantação")
    public ResponseEntity<List<InsumoResponse>> listarPorPlantacao(@PathVariable Long plantacaoId) {
        return ResponseEntity.ok(service.listarPorPlantacao(plantacaoId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar insumo")
    public ResponseEntity<EntityModel<InsumoResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody InsumoRequest request) {
        return ResponseEntity.ok(toModel(service.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar insumo")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<InsumoResponse> toModel(InsumoResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(InsumoController.class).buscarPorId(response.id())).withSelfRel(),
                linkTo(methodOn(InsumoController.class).listarTodos()).withRel("insumos"),
                linkTo(methodOn(PlantacaoController.class).buscarPorId(response.plantacaoId())).withRel("plantacao")
        );
    }
}
