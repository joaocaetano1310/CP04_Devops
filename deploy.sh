#!/usr/bin/env bash
# ========================================================================
# AgroVision - Provisionamento COMPLETO na Azure via Azure CLI
# ------------------------------------------------------------------------
# Cria, nesta ordem:
#   1. Resource Group
#   2. Azure Container Registry (ACR)
#   3. Storage Account + File Share  (volume persistente do banco)
#   4. Build e push das imagens para o ACR
#   5. ACI do banco de dados (MySQL, com volume persistente)
#   6. ACI da aplicacao (Spring Boot, com IP publico e DNS)
#
# Pre-requisitos:
#   - Azure CLI autenticado ......... az login
#   - Docker em execucao
#   - Arquivo .env preenchido ....... cp .env.example .env
#
# Uso:
#   chmod +x deploy.sh
#   ./deploy.sh
# ========================================================================

set -euo pipefail

# ------------------------------------------------------------------------
# Carrega as credenciais do arquivo .env (nao versionado)
# ------------------------------------------------------------------------
if [ ! -f .env ]; then
  echo "ERRO: arquivo .env nao encontrado."
  echo "Execute: cp .env.example .env  e preencha os valores."
  exit 1
fi
set -a; source .env; set +a

# ------------------------------------------------------------------------
# Parametros - RM do representante do grupo usado como prefixo
# ------------------------------------------------------------------------
RM="562074"
LOCATION="brazilsouth"

RESOURCE_GROUP="${RM}-grupo-rg"
ACR_NAME="acr${RM}agrovision"
STORAGE_ACCOUNT="st${RM}agrovision"
FILE_SHARE="dbdata"

APP_ACI="${RM}-app"
DB_ACI="${RM}-db"

APP_IMAGE="${RM}-app"
DB_IMAGE="${RM}-db"

DNS_APP="agrovision-app-${RM}"
DNS_DB="agrovision-db-${RM}"

echo "========================================================"
echo " AgroVision - Deploy na Azure"
echo " Resource Group : ${RESOURCE_GROUP}"
echo " Regiao         : ${LOCATION}"
echo "========================================================"

# ------------------------------------------------------------------------
# 1. Resource Group
# ------------------------------------------------------------------------
echo ""
echo ">>> [1/6] Criando Resource Group..."
az group create \
  --name "${RESOURCE_GROUP}" \
  --location "${LOCATION}" \
  --output table

# ------------------------------------------------------------------------
# 2. Azure Container Registry
# ------------------------------------------------------------------------
echo ""
echo ">>> [2/6] Criando Azure Container Registry..."
az acr create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${ACR_NAME}" \
  --sku Basic \
  --admin-enabled true \
  --output table

ACR_SERVER=$(az acr show --name "${ACR_NAME}" \
  --resource-group "${RESOURCE_GROUP}" \
  --query loginServer -o tsv)

ACR_USER=$(az acr credential show --name "${ACR_NAME}" \
  --query username -o tsv)

ACR_PASSWORD=$(az acr credential show --name "${ACR_NAME}" \
  --query "passwords[0].value" -o tsv)

# ------------------------------------------------------------------------
# 3. Storage Account + File Share (persistencia do banco)
# ------------------------------------------------------------------------
echo ""
echo ">>> [3/6] Criando Conta de Armazenamento e File Share..."
az storage account create \
  --name "${STORAGE_ACCOUNT}" \
  --resource-group "${RESOURCE_GROUP}" \
  --location "${LOCATION}" \
  --sku Standard_LRS \
  --kind StorageV2 \
  --output table

STORAGE_KEY=$(az storage account keys list \
  --account-name "${STORAGE_ACCOUNT}" \
  --resource-group "${RESOURCE_GROUP}" \
  --query "[0].value" -o tsv)

az storage share create \
  --name "${FILE_SHARE}" \
  --account-name "${STORAGE_ACCOUNT}" \
  --account-key "${STORAGE_KEY}" \
  --quota 10 \
  --output table

# ------------------------------------------------------------------------
# 4. Build e push das imagens
# ------------------------------------------------------------------------
echo ""
echo ">>> [4/6] Build das imagens e push para o ACR..."

az acr login --name "${ACR_NAME}"

docker build -t "${APP_IMAGE}:latest" .
docker build -t "${DB_IMAGE}:latest" ./db

docker tag "${APP_IMAGE}:latest" "${ACR_SERVER}/${APP_IMAGE}:latest"
docker tag "${DB_IMAGE}:latest"  "${ACR_SERVER}/${DB_IMAGE}:latest"

docker push "${ACR_SERVER}/${APP_IMAGE}:latest"
docker push "${ACR_SERVER}/${DB_IMAGE}:latest"

echo ""
echo "Imagens registradas no ACR:"
az acr repository list --name "${ACR_NAME}" --output table

# ------------------------------------------------------------------------
# 5. ACI do Banco de Dados - com volume persistente
# ------------------------------------------------------------------------
echo ""
echo ">>> [5/6] Criando o ACI do banco de dados..."
az container create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${DB_ACI}" \
  --image "${ACR_SERVER}/${DB_IMAGE}:latest" \
  --registry-login-server "${ACR_SERVER}" \
  --registry-username "${ACR_USER}" \
  --registry-password "${ACR_PASSWORD}" \
  --cpu 1 \
  --memory 2 \
  --os-type Linux \
  --ports 3306 \
  --ip-address Public \
  --dns-name-label "${DNS_DB}" \
  --secure-environment-variables \
      MYSQL_ROOT_PASSWORD="${DB_ROOT_PASSWORD}" \
      MYSQL_PASSWORD="${DB_PASSWORD}" \
  --environment-variables \
      MYSQL_DATABASE="${DB_NAME}" \
      MYSQL_USER="${DB_USER}" \
  --azure-file-volume-account-name "${STORAGE_ACCOUNT}" \
  --azure-file-volume-account-key "${STORAGE_KEY}" \
  --azure-file-volume-share-name "${FILE_SHARE}" \
  --azure-file-volume-mount-path /var/lib/mysql \
  --restart-policy OnFailure \
  --output table

DB_FQDN=$(az container show \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${DB_ACI}" \
  --query ipAddress.fqdn -o tsv)

echo "Banco de dados disponivel em: ${DB_FQDN}:3306"
echo "Aguardando a inicializacao do MySQL (60s)..."
sleep 60

# ------------------------------------------------------------------------
# 6. ACI da Aplicacao
# ------------------------------------------------------------------------
echo ""
echo ">>> [6/6] Criando o ACI da aplicacao..."
az container create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${APP_ACI}" \
  --image "${ACR_SERVER}/${APP_IMAGE}:latest" \
  --registry-login-server "${ACR_SERVER}" \
  --registry-username "${ACR_USER}" \
  --registry-password "${ACR_PASSWORD}" \
  --cpu 1 \
  --memory 2 \
  --os-type Linux \
  --ports 8080 \
  --ip-address Public \
  --dns-name-label "${DNS_APP}" \
  --secure-environment-variables \
      DB_PASSWORD="${DB_PASSWORD}" \
      JWT_SECRET="${JWT_SECRET}" \
  --environment-variables \
      SPRING_PROFILES_ACTIVE=docker \
      DB_HOST="${DB_FQDN}" \
      DB_PORT=3306 \
      DB_NAME="${DB_NAME}" \
      DB_USER="${DB_USER}" \
  --restart-policy OnFailure \
  --output table

APP_FQDN=$(az container show \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${APP_ACI}" \
  --query ipAddress.fqdn -o tsv)

# ------------------------------------------------------------------------
# Resumo
# ------------------------------------------------------------------------
echo ""
echo "========================================================"
echo " DEPLOY CONCLUIDO"
echo "========================================================"
echo " Resource Group ....... ${RESOURCE_GROUP}"
echo " ACR .................. ${ACR_SERVER}"
echo " Storage Account ...... ${STORAGE_ACCOUNT} (share: ${FILE_SHARE})"
echo " ACI Banco ............ ${DB_ACI}  -> ${DB_FQDN}:3306"
echo " ACI Aplicacao ........ ${APP_ACI} -> http://${APP_FQDN}:8080"
echo ""
echo " Swagger UI: http://${APP_FQDN}:8080/swagger-ui.html"
echo "========================================================"
