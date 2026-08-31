package br.com.fiap.agrovision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Log de erros de procedures Oracle — mapeado para TB_LOG_ERRO_GS.
 *
 * Colunas:
 *   ID_LOG          → id             (NUMBER GENERATED ALWAYS AS IDENTITY PK)
 *   NOME_PROCEDURE  → nomeProcedure  (VARCHAR2(100))
 *   NOME_USER_GS    → nomeUser       (VARCHAR2(50))
 *   DATA_ERRO       → dataErro       (DATE)
 *   CODIGO_ERRO     → codigoErro     (NUMBER)
 *   MENSAGEM_ERRO   → mensagemErro   (VARCHAR2(500))
 *
 * Esta tabela é geralmente escrita por procedures Oracle (EXCEPTION WHEN OTHERS),
 * mas também pode ser consultada pela API para fins de auditoria.
 */
@Entity
@Table(name = "TB_LOG_ERRO_GS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogErro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LOG")
    private Long id;

    @Column(name = "NOME_PROCEDURE", length = 100)
    private String nomeProcedure;

    @Column(name = "NOME_USER_GS", length = 50)
    private String nomeUser;

    @Column(name = "DATA_ERRO")
    private LocalDate dataErro;

    @Column(name = "CODIGO_ERRO")
    private Integer codigoErro;

    @Column(name = "MENSAGEM_ERRO", length = 500)
    private String mensagemErro;
}
