package br.com.fiap.agrovision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Insumo agrícola vinculado a uma plantação — mapeado para TB_INSUMO_GS.
 *
 * Colunas:
 *   ID_INSUMO     → id           (NUMBER PK)
 *   ID_PLANTACAO  → plantacao    (FK → TB_PLANTACOES_GS)
 *   NOME_INSUMO   → nomeInsumo   (VARCHAR2(50) NOT NULL)
 *   QTD_ESTOQUE   → qtdEstoque   (NUMBER(10,2) NOT NULL)
 */
@Entity
@Table(name = "TB_INSUMO_GS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Insumo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_INSUMO")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANTACAO", nullable = false)
    private Plantacao plantacao;

    @Column(name = "NOME_INSUMO", nullable = false, length = 50)
    private String nomeInsumo;

    @Column(name = "QTD_ESTOQUE", nullable = false, precision = 10, scale = 2)
    private BigDecimal qtdEstoque;
}
