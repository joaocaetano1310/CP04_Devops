package br.com.fiap.agrovision.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuário da fazenda — mapeado para TB_USER_GS.
 *
 * Colunas:
 *   ID_USER       → id        (NUMBER PK)
 *   CPF_USER      → cpf       (NUMBER(11) UNIQUE NOT NULL)
 *   NOME_USER     → nome      (VARCHAR2(60) NOT NULL)
 *   SENHA_USER    → senha     (VARCHAR2(18) — armazenamos o hash BCrypt truncado)
 *   NM_FAZENDA    → nomeFazenda (VARCHAR2(60) NOT NULL)
 *
 * Nota: o campo CPF_USER é NUMBER no Oracle; aqui usamos String para facilitar
 * a validação de formato (zeros à esquerda).
 */
@Entity
@Table(name = "TB_USER_GS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_USER")
	private Long id;
	
    /** CPF sem formatação — 11 dígitos */
    @Column(name = "CPF_USER", unique = true, nullable = false)
    private Long cpf;

    @Column(name = "NOME_USER", nullable = false, length = 60)
    private String nome;

    /**
     * Senha criptografada (BCrypt).
     * O Oracle limita a 18 chars; usamos VARCHAR2(100) para acomodar o hash —
     * o DDL do Oracle deve ser ajustado para VARCHAR2(100) neste campo.
     */
    @Column(name = "SENHA_USER", nullable = false, length = 100)
    private String senha;

    @Column(name = "NM_FAZENDA", nullable = false, length = 60)
    private String nomeFazenda;

    /* ------------------------------------------------------------------ */
    /*  Spring Security — UserDetails                                      */
    /* ------------------------------------------------------------------ */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /** CPF é o "username" usado na autenticação */
    @Override public String getUsername() { return String.valueOf(cpf); }

    @Override
    public String getPassword() { return senha; }

    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}
