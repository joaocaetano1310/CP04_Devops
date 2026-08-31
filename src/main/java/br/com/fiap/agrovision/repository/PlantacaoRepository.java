package br.com.fiap.agrovision.repository;

import br.com.fiap.agrovision.entity.Plantacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantacaoRepository extends JpaRepository<Plantacao, Long> {
    List<Plantacao> findByUsuarioId(Long usuarioId);
    List<Plantacao> findByStatus(Plantacao.StatusPlantio status);
    List<Plantacao> findByTipoPlantioContainingIgnoreCase(String tipoPlantio);
}
