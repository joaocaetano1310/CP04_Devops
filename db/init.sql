-- ========================================================================
-- AgroVision - Script DDL
-- Banco de dados: MySQL 8.0
--
-- Executado automaticamente pelo container na primeira inicializacao,
-- via /docker-entrypoint-initdb.d/
-- ========================================================================

CREATE DATABASE IF NOT EXISTS agrovision
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE agrovision;

-- ------------------------------------------------------------------------
-- TB_USER_GS - Usuarios (produtores rurais)
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS TB_USER_GS (
    ID_USER      BIGINT       NOT NULL AUTO_INCREMENT,
    CPF_USER     BIGINT       NOT NULL,
    NOME_USER    VARCHAR(60)  NOT NULL,
    SENHA_USER   VARCHAR(100) NOT NULL,
    NM_FAZENDA   VARCHAR(60)  NOT NULL,
    CONSTRAINT PK_USER_GS  PRIMARY KEY (ID_USER),
    CONSTRAINT UK_USER_CPF UNIQUE (CPF_USER)
) ENGINE = InnoDB;

-- ------------------------------------------------------------------------
-- TB_PLANTACOES_GS - Plantacoes monitoradas
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS TB_PLANTACOES_GS (
    ID_PLANTACAO   BIGINT      NOT NULL AUTO_INCREMENT,
    ID_USER        BIGINT      NOT NULL,
    TIPO_PLANTIO   VARCHAR(50) NOT NULL,
    AREA_PLANTIO   BIGINT      NOT NULL,
    DATA_PLANTIO   DATE        NOT NULL,
    LOCAL_PLANTIO  VARCHAR(20) NOT NULL,
    STATUS_PLANTIO VARCHAR(10) NULL,
    CONSTRAINT PK_PLANTACOES_GS  PRIMARY KEY (ID_PLANTACAO),
    CONSTRAINT FK_PLANTACAO_USER FOREIGN KEY (ID_USER)
        REFERENCES TB_USER_GS (ID_USER) ON DELETE CASCADE,
    CONSTRAINT CK_STATUS_PLANTIO
        CHECK (STATUS_PLANTIO IN ('PLANTADO', 'PREPARACAO', 'DESCANSO'))
) ENGINE = InnoDB;

-- ------------------------------------------------------------------------
-- TB_INSUMO_GS - Insumos por plantacao
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS TB_INSUMO_GS (
    ID_INSUMO    BIGINT        NOT NULL AUTO_INCREMENT,
    ID_PLANTACAO BIGINT        NOT NULL,
    NOME_INSUMO  VARCHAR(50)   NOT NULL,
    QTD_ESTOQUE  DECIMAL(10,2) NOT NULL,
    CONSTRAINT PK_INSUMO_GS       PRIMARY KEY (ID_INSUMO),
    CONSTRAINT FK_INSUMO_PLANTACAO FOREIGN KEY (ID_PLANTACAO)
        REFERENCES TB_PLANTACOES_GS (ID_PLANTACAO) ON DELETE CASCADE,
    CONSTRAINT CK_QTD_ESTOQUE CHECK (QTD_ESTOQUE >= 0)
) ENGINE = InnoDB;

-- ------------------------------------------------------------------------
-- TB_SAFRA_GS - Safras colhidas
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS TB_SAFRA_GS (
    ID_SAFRA      BIGINT        NOT NULL AUTO_INCREMENT,
    ID_PLANTACAO  BIGINT        NOT NULL,
    DATA_COLHEITA DATE          NOT NULL,
    QTD_COLHIDA   DECIMAL(15,2) NOT NULL,
    CONSTRAINT PK_SAFRA_GS        PRIMARY KEY (ID_SAFRA),
    CONSTRAINT FK_SAFRA_PLANTACAO FOREIGN KEY (ID_PLANTACAO)
        REFERENCES TB_PLANTACOES_GS (ID_PLANTACAO) ON DELETE CASCADE,
    CONSTRAINT CK_QTD_COLHIDA CHECK (QTD_COLHIDA > 0)
) ENGINE = InnoDB;

-- ------------------------------------------------------------------------
-- TB_RELATORIO_GS - Relatorios gerados
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS TB_RELATORIO_GS (
    ID_RELATORIO       BIGINT        NOT NULL AUTO_INCREMENT,
    TITULO_RELATORIO   VARCHAR(100)  NOT NULL,
    CONTEUDO_RELATORIO VARCHAR(1000) NOT NULL,
    DATA_GERACAO       DATE          NULL,
    CONSTRAINT PK_RELATORIO_GS PRIMARY KEY (ID_RELATORIO)
) ENGINE = InnoDB;

-- ------------------------------------------------------------------------
-- TB_LOG_ERRO_GS - Log de erros da aplicacao
-- ------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS TB_LOG_ERRO_GS (
    ID_LOG         BIGINT       NOT NULL AUTO_INCREMENT,
    NOME_PROCEDURE VARCHAR(100) NULL,
    NOME_USER_GS   VARCHAR(50)  NULL,
    DATA_ERRO      DATE         NULL,
    CODIGO_ERRO    INT          NULL,
    MENSAGEM_ERRO  VARCHAR(500) NULL,
    CONSTRAINT PK_LOG_ERRO_GS PRIMARY KEY (ID_LOG)
) ENGINE = InnoDB;

-- ------------------------------------------------------------------------
-- Indices auxiliares
-- ------------------------------------------------------------------------
CREATE INDEX IDX_PLANTACAO_USER   ON TB_PLANTACOES_GS (ID_USER);
CREATE INDEX IDX_INSUMO_PLANTACAO ON TB_INSUMO_GS      (ID_PLANTACAO);
CREATE INDEX IDX_SAFRA_PLANTACAO  ON TB_SAFRA_GS       (ID_PLANTACAO);
