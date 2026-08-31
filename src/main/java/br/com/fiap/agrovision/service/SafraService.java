package br.com.fiap.agrovision.service;

import br.com.fiap.agrovision.dto.request.SafraRequest;
import br.com.fiap.agrovision.dto.response.SafraResponse;
import br.com.fiap.agrovision.entity.Plantacao;
import br.com.fiap.agrovision.entity.Safra;
import br.com.fiap.agrovision.exception.ResourceNotFoundException;
import br.com.fiap.agrovision.repository.PlantacaoRepository;
import br.com.fiap.agrovision.repository.SafraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SafraService {

    private final SafraRepository repository;
    private final PlantacaoRepository plantacaoRepository;

    public SafraResponse criar(SafraRequest request) {
        Plantacao plantacao = findPlantacao(request.plantacaoId());

        Safra safra = Safra.builder()
                .plantacao(plantacao)
                .dataColheita(request.dataColheita())
                .qtdColhida(request.qtdColhida())
                .build();

        return toResponse(repository.save(safra));
    }

    public List<SafraResponse> listarTodas() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<SafraResponse> listarPorPlantacao(Long plantacaoId) {
        return repository.findByPlantacaoId(plantacaoId).stream().map(this::toResponse).toList();
    }

    public SafraResponse buscarPorId(Long id) {
        return toResponse(findById(id));
    }

    public SafraResponse atualizar(Long id, SafraRequest request) {
        Safra safra = findById(id);
        Plantacao plantacao = findPlantacao(request.plantacaoId());

        safra.setPlantacao(plantacao);
        safra.setDataColheita(request.dataColheita());
        safra.setQtdColhida(request.qtdColhida());

        return toResponse(repository.save(safra));
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    /** Total colhido de uma plantação (soma de todas as safras) */
    public BigDecimal totalColhidoPorPlantacao(Long plantacaoId) {
        return repository.somarColheitaPorPlantacao(plantacaoId);
    }

    private Safra findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safra não encontrada com id: " + id));
    }

    private Plantacao findPlantacao(Long plantacaoId) {
        return plantacaoRepository.findById(plantacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantação não encontrada com id: " + plantacaoId));
    }

    public SafraResponse toResponse(Safra s) {
        return new SafraResponse(
                s.getId(),
                s.getPlantacao().getId(),
                s.getPlantacao().getTipoPlantio(),
                s.getDataColheita(),
                s.getQtdColhida()
        );
    }
}
