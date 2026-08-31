package br.com.fiap.agrovision.service;

import br.com.fiap.agrovision.dto.request.InsumoRequest;
import br.com.fiap.agrovision.dto.response.InsumoResponse;
import br.com.fiap.agrovision.entity.Insumo;
import br.com.fiap.agrovision.entity.Plantacao;
import br.com.fiap.agrovision.exception.ResourceNotFoundException;
import br.com.fiap.agrovision.repository.InsumoRepository;
import br.com.fiap.agrovision.repository.PlantacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsumoService {

    private final InsumoRepository repository;
    private final PlantacaoRepository plantacaoRepository;

    public InsumoResponse criar(InsumoRequest request) {
        Plantacao plantacao = findPlantacao(request.plantacaoId());

        Insumo insumo = Insumo.builder()
                .plantacao(plantacao)
                .nomeInsumo(request.nomeInsumo())
                .qtdEstoque(request.qtdEstoque())
                .build();

        return toResponse(repository.save(insumo));
    }

    public List<InsumoResponse> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<InsumoResponse> listarPorPlantacao(Long plantacaoId) {
        return repository.findByPlantacaoId(plantacaoId).stream().map(this::toResponse).toList();
    }

    public InsumoResponse buscarPorId(Long id) {
        return toResponse(findById(id));
    }

    public InsumoResponse atualizar(Long id, InsumoRequest request) {
        Insumo insumo = findById(id);
        Plantacao plantacao = findPlantacao(request.plantacaoId());

        insumo.setPlantacao(plantacao);
        insumo.setNomeInsumo(request.nomeInsumo());
        insumo.setQtdEstoque(request.qtdEstoque());

        return toResponse(repository.save(insumo));
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    private Insumo findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo não encontrado com id: " + id));
    }

    private Plantacao findPlantacao(Long plantacaoId) {
        return plantacaoRepository.findById(plantacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantação não encontrada com id: " + plantacaoId));
    }

    public InsumoResponse toResponse(Insumo i) {
        return new InsumoResponse(
                i.getId(),
                i.getPlantacao().getId(),
                i.getPlantacao().getTipoPlantio(),
                i.getNomeInsumo(),
                i.getQtdEstoque()
        );
    }
}
