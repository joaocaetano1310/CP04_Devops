#!/bin/bash
# =========================================================================
# 1o Checkpoint 2o Semestre - Containers em Nuvem (ACR / ACI)
# Grupo CloudOps - RM 562766 (Felipe Furlanetto)
#
# Cria todos os recursos da solucao na Azure via Azure CLI:
#   Resource Group, Container Registry, Conta de Armazenamento,
#   File Share e dois Container Instances (aplicacao e banco de dados).
#
# A regiao eastus e usada porque a assinatura da FIAP restringe por
# politica as regioes disponiveis (brazilsouth e bloqueada).
#
# chmod +x deploy.sh
# sed -i 's/\r$//' deploy.sh
# ./deploy.sh
# =========================================================================

# Interrompe o script no primeiro erro, evitando falhas em cascata
set -e

# Variaveis principais
RM=562766
GRUPO=cloudops
CP=cp4
LOCATION=eastus

RG=rg-$RM-$CP
ACR=acr$RM$CP
STORAGE=st$RM$CP
SHARE=dbdata

ACI_APP=$RM-app
ACI_DB=$RM-db

IMG_APP=$RM-app
IMG_DB=$RM-db

DNS_APP=$CP-app-$RM
DNS_DB=$CP-db-$RM

# O 'az container create' nao aceita --tags, por isso as tags sao
# aplicadas apenas ao Resource Group, ao ACR e ao Armazenamento.
TAGS="owner=$GRUPO environment=dev cost-center=fiap"

# Credenciais informadas na execucao.
# Nao ficam gravadas no script nem no repositorio.
read -p  "Nome do banco de dados [agrovision]: " DB_NAME
DB_NAME=${DB_NAME:-agrovision}
read -p  "Usuario do banco [agrouser]: " DB_USER
DB_USER=${DB_USER:-agrouser}
read -sp "Senha do usuario do banco: " DB_PASSWORD; echo
read -sp "Senha do root do MySQL: "     DB_ROOT_PASSWORD; echo
read -sp "Chave JWT (min. 32 caracteres): " JWT_SECRET; echo

# 1. Resource Group
az group create \
  --name $RG \
  --location $LOCATION \
  --tags $TAGS

# 2. Azure Container Registry
az acr create \
  --resource-group $RG \
  --name $ACR \
  --sku Basic \
  --admin-enabled true \
  --tags $TAGS

# 3. Conta de Armazenamento
az storage account create \
  --resource-group $RG \
  --name $STORAGE \
  --location $LOCATION \
  --sku Standard_LRS \
  --kind StorageV2 \
  --tags $TAGS

# 4. File Share - volume persistente do banco de dados
# O 'tr -d' remove o retorno de carro que o Azure CLI adiciona no
# Windows/Git Bash e que invalidaria a assinatura das requisicoes.
STORAGE_KEY=$(az storage account keys list \
  --resource-group $RG \
  --account-name $STORAGE \
  --query "[0].value" -o tsv | tr -d '\r')

az storage share create \
  --name $SHARE \
  --account-name $STORAGE \
  --account-key $STORAGE_KEY \
  --quota 10

# 5. Build das imagens localmente
docker build -t $IMG_APP:latest .
docker build -t $IMG_DB:latest ./db

# 6. Login no ACR e push das imagens
az acr login --name $ACR

ACR_SERVER=$ACR.azurecr.io

docker tag $IMG_APP:latest $ACR_SERVER/$IMG_APP:latest
docker tag $IMG_DB:latest  $ACR_SERVER/$IMG_DB:latest

docker push $ACR_SERVER/$IMG_APP:latest
docker push $ACR_SERVER/$IMG_DB:latest

az acr repository list --name $ACR --output table

# 7. Credenciais do ACR para o ACI baixar as imagens
ACR_USER=$(az acr credential show \
  --name $ACR \
  --query username -o tsv | tr -d '\r')

ACR_PASSWORD=$(az acr credential show \
  --name $ACR \
  --query "passwords[0].value" -o tsv | tr -d '\r')

# 8. ACI do banco de dados - com volume persistente
az container create \
  --resource-group $RG \
  --name $ACI_DB \
  --image $ACR_SERVER/$IMG_DB:latest \
  --registry-login-server $ACR_SERVER \
  --registry-username $ACR_USER \
  --registry-password $ACR_PASSWORD \
  --cpu 1 \
  --memory 2 \
  --os-type Linux \
  --ports 3306 \
  --ip-address Public \
  --dns-name-label $DNS_DB \
  --environment-variables MYSQL_DATABASE=$DB_NAME MYSQL_USER=$DB_USER \
  --secure-environment-variables MYSQL_ROOT_PASSWORD=$DB_ROOT_PASSWORD MYSQL_PASSWORD=$DB_PASSWORD \
  --azure-file-volume-account-name $STORAGE \
  --azure-file-volume-account-key $STORAGE_KEY \
  --azure-file-volume-share-name $SHARE \
  --azure-file-volume-mount-path /var/lib/mysql \
  --restart-policy OnFailure

# 9. Endereco do banco e espera pela inicializacao do MySQL
DB_HOST=$(az container show \
  --resource-group $RG \
  --name $ACI_DB \
  --query ipAddress.fqdn -o tsv | tr -d '\r')

echo "Banco de dados em: $DB_HOST:3306"
echo "Aguardando a inicializacao do MySQL..."
sleep 60

# 10. ACI da aplicacao
az container create \
  --resource-group $RG \
  --name $ACI_APP \
  --image $ACR_SERVER/$IMG_APP:latest \
  --registry-login-server $ACR_SERVER \
  --registry-username $ACR_USER \
  --registry-password $ACR_PASSWORD \
  --cpu 1 \
  --memory 2 \
  --os-type Linux \
  --ports 8080 \
  --ip-address Public \
  --dns-name-label $DNS_APP \
  --environment-variables SPRING_PROFILES_ACTIVE=docker DB_HOST=$DB_HOST DB_PORT=3306 DB_NAME=$DB_NAME DB_USER=$DB_USER \
  --secure-environment-variables DB_PASSWORD=$DB_PASSWORD JWT_SECRET=$JWT_SECRET \
  --restart-policy OnFailure

# 11. Resultado final
APP_HOST=$(az container show \
  --resource-group $RG \
  --name $ACI_APP \
  --query ipAddress.fqdn -o tsv | tr -d '\r')

az resource list --resource-group $RG --output table

echo ""
echo "========================================================"
echo " Deploy concluido"
echo "========================================================"
echo " Resource Group ..... $RG"
echo " Container Registry . $ACR_SERVER"
echo " Armazenamento ...... $STORAGE (share: $SHARE)"
echo " ACI do banco ....... $ACI_DB  -> $DB_HOST:3306"
echo " ACI da aplicacao ... $ACI_APP -> http://$APP_HOST:8080"
echo ""
echo " Swagger: http://$APP_HOST:8080/swagger-ui.html"
echo "========================================================"
