package br.com.fiap.agrovision.controller;

import br.com.fiap.agrovision.dto.request.SafraRequest;
import br.com.fiap.agrovision.dto.response.SafraResponse;
import br.com.fiap.agrovision.service.SafraService;
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

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/safras")
@RequiredArgsConstructor
@Tag(name = "Safras", description = "Registro de colheitas por plantação (TB_SAFRA_GS)")
@SecurityRequirement(name = "bearerAuth")
public class SafraController {

    private final SafraService service;

    @PostMapping
    @Operation(summary = "Registrar safra (colheita)")
    public ResponseEntity<EntityModel<SafraResponse>> criar(@Valid @RequestBody SafraRequest request) {
        SafraResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(toModel(response));
    }

    @GetMapping
    @Operation(summary = "Listar todas as safras")
    public ResponseEntity<CollectionModel<EntityModel<SafraResponse>>> listarTodas() {
        List<EntityModel<SafraResponse>> models = service.listarTodas().stream()
                .map(this::toModel).toList();
        Link selfLink = linkTo(methodOn(SafraController.class).listarTodas()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(models, selfLink));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar safra por ID")
    public ResponseEntity<EntityModel<SafraResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(service.buscarPorId(id)));
    }

    @GetMapping("/plantacao/{plantacaoId}")
    @Operation(summary = "Listar safras por plantação")
    public ResponseEntity<List<SafraResponse>> listarPorPlantacao(@PathVariable Long plantacaoId) {
        return ResponseEntity.ok(service.listarPorPlantacao(plantacaoId));
    }

    @GetMapping("/plantacao/{plantacaoId}/total")
    @Operation(
            summary = "Total colhido por plantação",
            description = "Soma de QTD_COLHIDA de todas as safras de uma plantação."
    )
    public ResponseEntity<BigDecimal> totalColhido(@PathVariable Long plantacaoId) {
        return ResponseEntity.ok(service.totalColhidoPorPlantacao(plantacaoId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar safra")
    public ResponseEntity<EntityModel<SafraResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody SafraRequest request) {
        return ResponseEntity.ok(toModel(service.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar safra")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<SafraResponse> toModel(SafraResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(SafraController.class).buscarPorId(response.id())).withSelfRel(),
                linkTo(methodOn(SafraController.class).listarTodas()).withRel("safras"),
                linkTo(methodOn(PlantacaoController.class).buscarPorId(response.plantacaoId())).withRel("plantacao")
        );
    }
}
