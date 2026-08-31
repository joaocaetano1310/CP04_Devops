package br.com.fiap.agrovision.controller;

import br.com.fiap.agrovision.dto.request.UsuarioRequest;
import br.com.fiap.agrovision.dto.response.UsuarioResponse;
import br.com.fiap.agrovision.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gestão de usuários da fazenda (TB_USER_GS)")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    @Operation(summary = "Cadastrar usuário", description = "Endpoint público — não requer token JWT.")
    public ResponseEntity<EntityModel<UsuarioResponse>> criar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(toModel(response));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioResponse>>> listarTodos() {
        List<EntityModel<UsuarioResponse>> models = service.listarTodos().stream()
                .map(this::toModel).toList();
        Link selfLink = linkTo(methodOn(UsuarioController.class).listarTodos()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(models, selfLink));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<EntityModel<UsuarioResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<EntityModel<UsuarioResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(toModel(service.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<UsuarioResponse> toModel(UsuarioResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(UsuarioController.class).buscarPorId(response.id())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarTodos()).withRel("usuarios"),
                linkTo(methodOn(PlantacaoController.class).listarPorUsuario(response.id())).withRel("plantacoes")
        );
    }
}
