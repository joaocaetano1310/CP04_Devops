# ========================================================================
# Imagem do banco de dados AgroVision - MySQL 8.0
# ------------------------------------------------------------------------
# O script init.sql e executado automaticamente na primeira inicializacao
# do container, criando o schema e todas as tabelas do projeto.
#
# Nenhuma senha esta gravada nesta imagem: as credenciais sao injetadas
# por variavel de ambiente no momento do deploy (docker run / az container).
# ========================================================================
FROM mysql:8.0

# Script de criacao do schema (DDL)
COPY init.sql /docker-entrypoint-initdb.d/01-init.sql

EXPOSE 3306
