package br.com.fiap.agrovision.repository;

import br.com.fiap.agrovision.entity.LogErro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogErroRepository extends JpaRepository<LogErro, Long> {
    List<LogErro> findByNomeUser(String nomeUser);
    List<LogErro> findByNomeProcedure(String nomeProcedure);
}
