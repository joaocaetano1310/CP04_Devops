package br.com.fiap.agrovision.repository;

import br.com.fiap.agrovision.entity.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    List<Insumo> findByPlantacaoId(Long plantacaoId);
    List<Insumo> findByNomeInsumoContainingIgnoreCase(String nomeInsumo);
}
