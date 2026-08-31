package br.com.fiap.agrovision.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

/**
 * Relatório gerado pela aplicação — mapeado para TB_RELATORIO_GS.
 *
 * Colunas:
 *   ID_RELATORIO       → id                 (NUMBER GENERATED ALWAYS AS IDENTITY PK)
 *   TITULO_RELATORIO   → titulo             (VARCHAR2(100) NOT NULL)
 *   CONTEUDO_RELATORIO → conteudo           (VARCHAR2(1000) NOT NULL)
 *   DATA_GERACAO       → dataGeracao        (DATE DEFAULT SYSDATE)
 *
 * O PK usa GENERATED ALWAYS AS IDENTITY no Oracle →
 * GenerationType.IDENTITY no JPA (não precisa de sequence explícita).
 */
@Entity
@Table(name = "TB_RELATORIO_GS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RELATORIO")
    private Long id;

    @Column(name = "TITULO_RELATORIO", nullable = false, length = 100)
    private String titulo;

    @Column(name = "CONTEUDO_RELATORIO", nullable = false, length = 1000)
    private String conteudo;

    /**
     * Preenchida automaticamente pelo banco (DEFAULT SYSDATE) ou pela aplicação.
     */
    @Column(name = "DATA_GERACAO", updatable = false)
    @CreationTimestamp
    private LocalDate dataGeracao;
}
