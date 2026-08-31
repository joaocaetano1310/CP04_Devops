package br.com.fiap.agrovision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Plantação monitorada — mapeada para TB_PLANTACOES_GS.
 *
 * Colunas:
 *   ID_PLANTACAO   → id            (NUMBER PK)
 *   ID_USER        → usuario       (FK → TB_USER_GS)
 *   TIPO_PLANTIO   → tipoPlantio   (VARCHAR2(50) NOT NULL)
 *   AREA_PLANTIO   → areaPlantio   (NUMBER(15) NOT NULL)
 *   DATA_PLANTIO   → dataPlantio   (DATE NOT NULL)
 *   LOCAL_PLANTIO  → localPlantio  (VARCHAR2(20) NOT NULL)
 *   STATUS_PLANTIO → status        (CHECK: PLANTADO | PREPARACAO | DESCANSO)
 */
@Entity
@Table(name = "TB_PLANTACOES_GS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Plantacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_PLANTACAO")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_USER", nullable = false)
	private Usuario usuario;
	
    @Column(name = "TIPO_PLANTIO", nullable = false, length = 50)
    private String tipoPlantio;

    /** Área em metros quadrados (NUMBER(15)) */
    @Column(name = "AREA_PLANTIO", nullable = false, precision = 15)
    private Long areaPlantio;

    @Column(name = "DATA_PLANTIO", nullable = false)
    private LocalDate dataPlantio;

    @Column(name = "LOCAL_PLANTIO", nullable = false, length = 20)
    private String localPlantio;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_PLANTIO", length = 10)
    private StatusPlantio status;

    /* ------------------------------------------------------------------ */
    /*  Relacionamentos filhos                                             */
    /* ------------------------------------------------------------------ */

    @OneToMany(mappedBy = "plantacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Safra> safras;

    @OneToMany(mappedBy = "plantacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Insumo> insumos;

    /* ------------------------------------------------------------------ */

    public enum StatusPlantio {
        PLANTADO, PREPARACAO, DESCANSO
    }
}
