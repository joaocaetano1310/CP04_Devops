package br.com.fiap.agrovision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de colheita (safra) — mapeado para TB_SAFRA_GS.
 *
 * Colunas:
 *   ID_SAFRA      → id            (NUMBER PK)
 *   ID_PLANTACAO  → plantacao     (FK → TB_PLANTACOES_GS)
 *   DATA_COLHEITA → dataColheita  (DATE NOT NULL)
 *   QTD_COLHIDA   → qtdColhida    (NUMBER(15,2) NOT NULL — kg, toneladas, etc.)
 */
@Entity
@Table(name = "TB_SAFRA_GS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Safra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_SAFRA")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANTACAO", nullable = false)
    private Plantacao plantacao;

    @Column(name = "DATA_COLHEITA", nullable = false)
    private LocalDate dataColheita;

    @Column(name = "QTD_COLHIDA", nullable = false, precision = 15, scale = 2)
    private BigDecimal qtdColhida;
}
