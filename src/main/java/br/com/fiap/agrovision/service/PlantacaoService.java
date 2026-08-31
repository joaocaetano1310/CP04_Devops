package br.com.fiap.agrovision.service;

import br.com.fiap.agrovision.dto.request.PlantacaoRequest;
import br.com.fiap.agrovision.dto.response.PlantacaoResponse;
import br.com.fiap.agrovision.entity.Plantacao;
import br.com.fiap.agrovision.entity.Usuario;
import br.com.fiap.agrovision.exception.ResourceNotFoundException;
import br.com.fiap.agrovision.repository.PlantacaoRepository;
import br.com.fiap.agrovision.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantacaoService {

    private final PlantacaoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public PlantacaoResponse criar(PlantacaoRequest request) {
        Usuario usuario = findUsuario(request.usuarioId());

        Plantacao plantacao = Plantacao.builder()
                .usuario(usuario)
                .tipoPlantio(request.tipoPlantio())
                .areaPlantio(request.areaPlantio())
                .dataPlantio(request.dataPlantio())
                .localPlantio(request.localPlantio())
                .status(request.status() != null ? request.status() : Plantacao.StatusPlantio.PREPARACAO)
                .build();

        return toResponse(repository.save(plantacao));
    }

    public List<PlantacaoResponse> listarTodas() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<PlantacaoResponse> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    public PlantacaoResponse buscarPorId(Long id) {
        return toResponse(findById(id));
    }

    public PlantacaoResponse atualizar(Long id, PlantacaoRequest request) {
        Plantacao plantacao = findById(id);
        Usuario usuario = findUsuario(request.usuarioId());

        plantacao.setUsuario(usuario);
        plantacao.setTipoPlantio(request.tipoPlantio());
        plantacao.setAreaPlantio(request.areaPlantio());
        plantacao.setDataPlantio(request.dataPlantio());
        plantacao.setLocalPlantio(request.localPlantio());
        if (request.status() != null) {
            plantacao.setStatus(request.status());
        }

        return toResponse(repository.save(plantacao));
    }

    /** PATCH — altera apenas o status da plantação */
    public PlantacaoResponse atualizarStatus(Long id, Plantacao.StatusPlantio novoStatus) {
        Plantacao plantacao = findById(id);
        plantacao.setStatus(novoStatus);
        return toResponse(repository.save(plantacao));
    }

    public void deletar(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    private Plantacao findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantação não encontrada com id: " + id));
    }

    private Usuario findUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + usuarioId));
    }

    public PlantacaoResponse toResponse(Plantacao p) {
        return new PlantacaoResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNome(),
                p.getUsuario().getNomeFazenda(),
                p.getTipoPlantio(),
                p.getAreaPlantio(),
                p.getDataPlantio(),
                p.getLocalPlantio(),
                p.getStatus()
        );
    }
}
