package br.com.fiap.agrovision.service;

import br.com.fiap.agrovision.dto.request.RelatorioRequest;
import br.com.fiap.agrovision.dto.response.RelatorioResponse;
import br.com.fiap.agrovision.entity.Relatorio;
import br.com.fiap.agrovision.exception.ResourceNotFoundException;
import br.com.fiap.agrovision.repository.RelatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final RelatorioRepository repository;

    public RelatorioResponse criar(RelatorioRequest request) {
        Relatorio relatorio = Relatorio.builder()
                .titulo(request.titulo())
                .conteudo(request.conteudo())
                .build();

        return toResponse(repository.save(relatorio));
    }

    public List<RelatorioResponse> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public RelatorioResponse buscarPorId(Long id) {
        return toResponse(findById(id));
    }

    public RelatorioResponse atualizar(Long id, RelatorioRequest request) {
        Relatorio relatorio = findById(id);
        relatorio.setTitulo(request.titulo());
        relatorio.setConteudo(request.conteudo());
        return toResponse(repository.save(relatorio));
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    private Relatorio findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatório não encontrado com id: " + id));
    }

    public RelatorioResponse toResponse(Relatorio r) {
        return new RelatorioResponse(r.getId(), r.getTitulo(), r.getConteudo(), r.getDataGeracao());
    }
}
