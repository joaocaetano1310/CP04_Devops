package br.com.fiap.agrovision.repository;

import br.com.fiap.agrovision.entity.Safra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SafraRepository extends JpaRepository<Safra, Long> {
    List<Safra> findByPlantacaoId(Long plantacaoId);

    /** Soma total colhida por plantação */
    @Query("SELECT COALESCE(SUM(s.qtdColhida), 0) FROM Safra s WHERE s.plantacao.id = :plantacaoId")
    BigDecimal somarColheitaPorPlantacao(Long plantacaoId);
}
