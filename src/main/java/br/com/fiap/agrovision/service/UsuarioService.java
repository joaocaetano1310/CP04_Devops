package br.com.fiap.agrovision.service;

import br.com.fiap.agrovision.dto.request.UsuarioRequest;
import br.com.fiap.agrovision.dto.response.UsuarioResponse;
import br.com.fiap.agrovision.entity.Usuario;
import br.com.fiap.agrovision.exception.ConflictException;
import br.com.fiap.agrovision.exception.ResourceNotFoundException;
import br.com.fiap.agrovision.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse criar(UsuarioRequest request) {
        if (repository.existsByCpf(request.cpf())) {
            throw new ConflictException("CPF já cadastrado: " + request.cpf());
        }

        Usuario usuario = Usuario.builder()
                .cpf(request.cpf())
                .nome(request.nome())
                .senha(passwordEncoder.encode(request.senha()))
                .nomeFazenda(request.nomeFazenda())
                .build();

        return toResponse(repository.save(usuario));
    }

    public List<UsuarioResponse> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponse buscarPorId(Long id) {
        return toResponse(findById(id));
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = findById(id);

        if (!usuario.getCpf().equals(request.cpf()) && repository.existsByCpf(request.cpf())) {
            throw new ConflictException("CPF já cadastrado: " + request.cpf());
        }

        usuario.setCpf(request.cpf());
        usuario.setNome(request.nome());
        usuario.setNomeFazenda(request.nomeFazenda());
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        return toResponse(repository.save(usuario));
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    private Usuario findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
    }

    public UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getCpf(), u.getNome(), u.getNomeFazenda());
    }
}