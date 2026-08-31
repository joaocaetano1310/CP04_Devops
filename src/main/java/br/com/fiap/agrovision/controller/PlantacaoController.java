package br.com.fiap.agrovision.controller;

import br.com.fiap.agrovision.dto.request.PlantacaoRequest;
import br.com.fiap.agrovision.dto.response.PlantacaoResponse;
import br.com.fiap.agrovision.entity.Plantacao;
import br.com.fiap.agrovision.service.PlantacaoService;
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
@RequestMapping("/api/plantacoes")
@RequiredArgsConstructor
@Tag(name = "Plantações", description = "Gestão de plantações monitoradas (TB_PLANTACOES_GS)")
@SecurityRequirement(name = "bearerAuth")
public class PlantacaoController {

    private final PlantacaoService service;

    @PostMapping
    @Operation(summary = "Cadastrar plantação")
    public ResponseEntity<EntityModel<PlantacaoResponse>> criar(@Valid @RequestBody PlantacaoRequest request) {
        PlantacaoResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(toModel(response));
    }

    @GetMapping
    @Operation(summary = "Listar todas as plantações")
    public ResponseEntity<CollectionModel<EntityModel<PlantacaoResponse>>> listarTodas() {
        List<EntityModel<PlantacaoResponse>> models = service.listarTodas().stream()
                .map(this::toModel).toList();
        Link selfLink = linkTo(methodOn(PlantacaoController.class).listarTodas()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(models, selfLink));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar plantação por ID")
    public ResponseEntity<EntityModel<PlantacaoResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(service.buscarPorId(id)));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar plantações por usuário")
    public ResponseEntity<List<PlantacaoResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar plantação")
    public ResponseEntity<EntityModel<PlantacaoResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody PlantacaoRequest request) {
        return ResponseEntity.ok(toModel(service.atualizar(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Alterar status da plantação",
            description = "Atualiza STATUS_PLANTIO para PLANTADO, PREPARACAO ou DESCANSO."
    )
    public ResponseEntity<EntityModel<PlantacaoResponse>> atualizarStatus(
            @PathVariable Long id,
            @RequestParam Plantacao.StatusPlantio status) {
        return ResponseEntity.ok(toModel(service.atualizarStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar plantação")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PlantacaoResponse> toModel(PlantacaoResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(PlantacaoController.class).buscarPorId(response.id())).withSelfRel(),
                linkTo(methodOn(PlantacaoController.class).listarTodas()).withRel("plantacoes"),
                linkTo(methodOn(UsuarioController.class).buscarPorId(response.usuarioId())).withRel("usuario"),
                linkTo(methodOn(SafraController.class).listarPorPlantacao(response.id())).withRel("safras"),
                linkTo(methodOn(InsumoController.class).listarPorPlantacao(response.id())).withRel("insumos")
        );
    }
}
