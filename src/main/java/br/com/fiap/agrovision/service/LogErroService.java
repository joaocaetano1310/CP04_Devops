package br.com.fiap.agrovision.service;

import br.com.fiap.agrovision.dto.response.LogErroResponse;
import br.com.fiap.agrovision.entity.LogErro;
import br.com.fiap.agrovision.exception.ResourceNotFoundException;
import br.com.fiap.agrovision.repository.LogErroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogErroService {

    private final LogErroRepository repository;

    public List<LogErroResponse> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public LogErroResponse buscarPorId(Long id) {
        return toResponse(findById(id));
    }

    public List<LogErroResponse> listarPorUsuario(String nomeUser) {
        return repository.findByNomeUser(nomeUser).stream().map(this::toResponse).toList();
    }

    public List<LogErroResponse> listarPorProcedure(String nomeProcedure) {
        return repository.findByNomeProcedure(nomeProcedure).stream().map(this::toResponse).toList();
    }

    private LogErro findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log de erro não encontrado com id: " + id));
    }

    public LogErroResponse toResponse(LogErro l) {
        return new LogErroResponse(
                l.getId(),
                l.getNomeProcedure(),
                l.getNomeUser(),
                l.getDataErro(),
                l.getCodigoErro(),
                l.getMensagemErro()
        );
    }
}
