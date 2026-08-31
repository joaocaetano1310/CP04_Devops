package br.com.fiap.agrovision;

import br.com.fiap.agrovision.dto.request.UsuarioRequest;
import br.com.fiap.agrovision.dto.response.UsuarioResponse;
import br.com.fiap.agrovision.entity.Usuario;
import br.com.fiap.agrovision.exception.ConflictException;
import br.com.fiap.agrovision.exception.ResourceNotFoundException;
import br.com.fiap.agrovision.repository.UsuarioRepository;
import br.com.fiap.agrovision.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository repository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private Usuario usuario;
    private UsuarioRequest request;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .cpf(12345678901L)
                .nome("João Silva")
                .senha("hashed_password")
                .nomeFazenda("Fazenda Esperança")
                .build();

        request = new UsuarioRequest(12345678901L, "João Silva", "senha123", "Fazenda Esperança");
    }

    @Test
    void criar_deveSalvarUsuarioComSucesso() {
        when(repository.existsByCpf(12345678901L)).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed_password");
        when(repository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponse response = service.criar(request);

        assertThat(response.cpf()).isEqualTo(12345678901L);
        assertThat(response.nome()).isEqualTo("João Silva");
        assertThat(response.nomeFazenda()).isEqualTo("Fazenda Esperança");
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void criar_deveLancarConflictException_quandoCpfJaExiste() {
        when(repository.existsByCpf(12345678901L)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("12345678901");
    }

    @Test
    void listarTodos_deveRetornarListaDeUsuarios() {
        when(repository.findAll()).thenReturn(List.of(usuario));

        List<UsuarioResponse> result = service.listarTodos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("João Silva");
    }

    @Test
    void buscarPorId_deveRetornarUsuario_quandoIdExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = service.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_deveLancarResourceNotFoundException_quandoIdNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deletar_deveRemoverUsuario_quandoIdExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        service.deletar(1L);

        verify(repository).deleteById(1L);
    }
}