package br.com.fiap.agrovision.controller;

import br.com.fiap.agrovision.dto.request.LoginRequest;
import br.com.fiap.agrovision.dto.response.TokenResponse;
import br.com.fiap.agrovision.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login via CPF e senha — retorna token JWT")
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    @Operation(
            summary = "Realizar login",
            description = "Autentica o usuário pelo CPF e senha. Retorna o token JWT para uso nos demais endpoints."
    )
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }
}
