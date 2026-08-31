package br.com.fiap.agrovision.repository;

import br.com.fiap.agrovision.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCpf(Long cpf);
    boolean existsByCpf(Long cpf);
}