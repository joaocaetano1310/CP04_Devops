package br.com.fiap.agrovision.service;

import br.com.fiap.agrovision.dto.request.LoginRequest;
import br.com.fiap.agrovision.dto.response.TokenResponse;
import br.com.fiap.agrovision.entity.Usuario;
import br.com.fiap.agrovision.repository.UsuarioRepository;
import br.com.fiap.agrovision.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(String.valueOf(request.cpf()), request.senha())
        );

        Usuario usuario = repository.findByCpf(request.cpf())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String token = jwtService.generateToken(usuario);
        return new TokenResponse(token, "Bearer", usuario.getCpf(), usuario.getNome(), usuario.getNomeFazenda());
    }
}