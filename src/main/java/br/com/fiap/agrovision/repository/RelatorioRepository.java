package br.com.fiap.agrovision.repository;

import br.com.fiap.agrovision.entity.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {
    List<Relatorio> findByTituloContainingIgnoreCase(String titulo);
}
