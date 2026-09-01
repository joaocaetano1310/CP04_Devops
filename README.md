# CP4 — Imagem e Containers em Nuvem (ACR / ACI)

**FIAP — Tecnologia em Desenvolvimento de Sistemas**
DevOps Tools & Cloud Computing — 1º Checkpoint, 2º Semestre
Projeto DimDim · Prof. João Carlos Menk

---

## Grupo CloudOps

| RM | Nome completo |
|---|---|
| 562766 | Felipe Furlanetto *(representante)* |
| 562074 | João Victor Caetano Alves da Silva |
| 564115 | João Victor Bueno Castelini da Silva |
| 565667 | Ryan Vetoriano |
| 564002 | Raul Rezende Iemini Aguiar |

---

## Objetivo do checkpoint

Containerizar uma aplicação Java e um banco de dados relacional, registrar as duas
imagens no **Azure Container Registry** e executá-las na nuvem como **dois Azure
Container Instances independentes**, com os dados do banco persistidos em uma Conta
de Armazenamento.

Não há máquina virtual envolvida: o ACI executa os containers diretamente, sem
servidor a administrar.

## A solução

A aplicação containerizada é uma API REST desenvolvida nas aulas de Java, com CRUD
sobre seis tabelas relacionais e autenticação JWT.

| Camada | Tecnologia |
|---|---|
| Aplicação | Java 21 · Spring Boot 3.3 · Spring Data JPA · JWT · Swagger |
| Banco de dados | MySQL 8.0 em container (**não** H2) |
| Imagem da aplicação | `eclipse-temurin:21-jre-alpine`, usuário não-root |
| Registro de imagens | Azure Container Registry |
| Execução | Azure Container Instances |
| Persistência | Azure Storage Account + File Share em `/var/lib/mysql` |
| Provisionamento | Azure CLI — `deploy.sh` |

```
   Máquina local                          Azure  ·  rg-562766-cp4
   ─────────────                          ──────────────────────────────────
   docker build ─── push ──►   ACR  acr562766cp4
                                 │
                                 │ pull
                                 ▼
                        ┌─────────────────┐      ┌─────────────────┐
                        │  ACI 562766-app │─────►│  ACI 562766-db  │
                        │  Spring Boot    │      │  MySQL 8.0      │
                        │  porta 8080     │      │  porta 3306     │
                        └────────┬────────┘      └────────┬────────┘
                                 │ IP público             │ volume
                                 ▼                        ▼
                              Internet             Storage Account
                                                   st562766cp4
                                                   share: dbdata
```

### Recursos criados

| Recurso | Nome |
|---|---|
| Resource Group | `rg-562766-cp4` |
| Container Registry | `acr562766cp4` |
| Storage Account | `st562766cp4` |
| File Share | `dbdata` |
| Container Instance — aplicação | `562766-app` |
| Container Instance — banco | `562766-db` |
| Imagem — aplicação | `562766-app:latest` |
| Imagem — banco | `562766-db:latest` |

Região: **eastus**.

### Tabelas

| Tabela | Descrição |
|---|---|
| `TB_USER_GS` | Usuários (produtores rurais) |
| `TB_PLANTACOES_GS` | Plantações monitoradas |
| `TB_INSUMO_GS` | Insumos por plantação |
| `TB_SAFRA_GS` | Safras colhidas |
| `TB_RELATORIO_GS` | Relatórios gerados |
| `TB_LOG_ERRO_GS` | Log de erros da aplicação |

DDL completo em [`db/init.sql`](db/init.sql), executado automaticamente na primeira
inicialização do container do banco.

### Estrutura do repositório

```
CP04_Devops/
├── src/                     código-fonte Java / Spring Boot
├── db/
│   ├── Dockerfile           imagem do MySQL 8.0
│   └── init.sql             DDL das 6 tabelas
├── testes/                  JSONs de GET, POST, PUT e DELETE
├── Dockerfile               imagem da aplicação (usuário não-root)
├── docker-compose.yml       ambiente local de validação
├── deploy.sh                criação de TODOS os recursos via Azure CLI
├── .env.example             modelo das variáveis de ambiente
├── pom.xml
└── README.md
```

---

# How To — Tutorial de execução

Todos os comandos abaixo, na ordem em que devem ser executados.

## Pré-requisitos

| Ferramenta | Onde obter |
|---|---|
| Docker Desktop | https://www.docker.com/products/docker-desktop |
| Azure CLI | https://aka.ms/installazurecliwindows |
| Git (com Git Bash) | https://git-scm.com/download/win |

No Windows, **todos os comandos rodam no Git Bash** — o `deploy.sh` usa `read -sp` e
`sed`, que não existem no PowerShell.

Conferir o ambiente:

```bash
az --version
docker ps
```

Se `docker ps` responder *cannot connect to the Docker daemon*, abra o Docker Desktop
e espere o ícone da baleia ficar verde.

---

## Passo 1 — Clonar o repositório

```bash
git clone https://github.com/joaocaetano1310/CP04_Devops.git
cd CP04_Devops
```

---

## Passo 2 — Validar localmente

Nenhuma credencial está gravada no código. Para o ambiente local, o Docker Compose
lê um arquivo `.env` que **não é versionado**:

```bash
cp .env.example .env
```

Subir os dois containers:

```bash
docker compose up -d --build
```

Conferir:

```bash
docker compose ps
docker compose logs -f app
```

A aplicação está pronta quando aparecer:

```
Started AgroVisionApplication in X.XXX seconds
```

Testar o CRUD localmente:

```bash
# Criar usuário (endpoint público)
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d @testes/01_usuario_post.json

# Autenticar e obter o token JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d @testes/02_auth_login.json
```

Conferir a gravação direto no banco:

```bash
docker exec -it 562766-db-local mysql -u agrouser -p agrovision -e "SELECT * FROM TB_USER_GS;"
```

Encerrar o ambiente local:

```bash
docker compose down
```

> Se não funciona localmente, não suba para a nuvem.

---

## Passo 3 — Build das imagens

```bash
# Imagem da aplicação
docker build -t 562766-app:latest .

# Imagem do banco de dados
docker build -t 562766-db:latest ./db
```

Conferir:

```bash
docker images | grep 562766
```

---

## Passo 4 — Login na Azure

```bash
az login
```

Confirmar a assinatura ativa:

```bash
az account show --output table
```

Se for a assinatura errada:

```bash
az account list --output table
az account set --subscription "<nome da assinatura>"
```

---

## Passo 5 — Criar os recursos na Azure

Os comandos desta seção estão todos dentro do [`deploy.sh`](deploy.sh) e são
executados por ele. Estão reproduzidos aqui para referência.

### 5.1 — Resource Group

```bash
az group create \
  --name rg-562766-cp4 \
  --location eastus \
  --tags owner=cloudops environment=dev cost-center=fiap
```

### 5.2 — Azure Container Registry

```bash
az acr create \
  --resource-group rg-562766-cp4 \
  --name acr562766cp4 \
  --sku Basic \
  --admin-enabled true \
  --tags owner=cloudops environment=dev cost-center=fiap
```

### 5.3 — Conta de Armazenamento

```bash
az storage account create \
  --resource-group rg-562766-cp4 \
  --name st562766cp4 \
  --location eastus \
  --sku Standard_LRS \
  --kind StorageV2 \
  --tags owner=cloudops environment=dev cost-center=fiap
```

### 5.4 — File Share (volume persistente do banco)

```bash
STORAGE_KEY=$(az storage account keys list \
  --resource-group rg-562766-cp4 \
  --account-name st562766cp4 \
  --query "[0].value" -o tsv | tr -d '\r')

az storage share create \
  --name dbdata \
  --account-name st562766cp4 \
  --account-key "$STORAGE_KEY" \
  --quota 10
```

---

## Passo 6 — Push das imagens para o ACR

```bash
az acr login --name acr562766cp4
```

```bash
docker tag 562766-app:latest acr562766cp4.azurecr.io/562766-app:latest
docker tag 562766-db:latest  acr562766cp4.azurecr.io/562766-db:latest

docker push acr562766cp4.azurecr.io/562766-app:latest
docker push acr562766cp4.azurecr.io/562766-db:latest
```

Conferir as imagens registradas:

```bash
az acr repository list --name acr562766cp4 --output table
```

---

## Passo 7 — Criar os Container Instances

Credenciais do ACR, para o ACI baixar as imagens:

```bash
ACR_USER=$(az acr credential show \
  --name acr562766cp4 --query username -o tsv | tr -d '\r')

ACR_PASSWORD=$(az acr credential show \
  --name acr562766cp4 --query "passwords[0].value" -o tsv | tr -d '\r')
```

### 7.1 — ACI do banco de dados

```bash
az container create \
  --resource-group rg-562766-cp4 \
  --name 562766-db \
  --image acr562766cp4.azurecr.io/562766-db:latest \
  --registry-login-server acr562766cp4.azurecr.io \
  --registry-username "$ACR_USER" \
  --registry-password "$ACR_PASSWORD" \
  --cpu 1 --memory 2 --os-type Linux \
  --ports 3306 \
  --ip-address Public \
  --dns-name-label cp4-db-562766 \
  --environment-variables MYSQL_DATABASE=agrovision MYSQL_USER=agrouser \
  --secure-environment-variables MYSQL_ROOT_PASSWORD="$DB_ROOT_PASSWORD" MYSQL_PASSWORD="$DB_PASSWORD" \
  --azure-file-volume-account-name st562766cp4 \
  --azure-file-volume-account-key "$STORAGE_KEY" \
  --azure-file-volume-share-name dbdata \
  --azure-file-volume-mount-path /var/lib/mysql \
  --restart-policy OnFailure
```

Endereço do banco, que será injetado na aplicação:

```bash
DB_HOST=$(az container show \
  --resource-group rg-562766-cp4 \
  --name 562766-db \
  --query ipAddress.fqdn -o tsv | tr -d '\r')

echo $DB_HOST
```

Aguardar o MySQL executar o `init.sql` e ficar disponível:

```bash
sleep 60
```

### 7.2 — ACI da aplicação

```bash
az container create \
  --resource-group rg-562766-cp4 \
  --name 562766-app \
  --image acr562766cp4.azurecr.io/562766-app:latest \
  --registry-login-server acr562766cp4.azurecr.io \
  --registry-username "$ACR_USER" \
  --registry-password "$ACR_PASSWORD" \
  --cpu 1 --memory 2 --os-type Linux \
  --ports 8080 \
  --ip-address Public \
  --dns-name-label cp4-app-562766 \
  --environment-variables SPRING_PROFILES_ACTIVE=docker DB_HOST="$DB_HOST" DB_PORT=3306 DB_NAME=agrovision DB_USER=agrouser \
  --secure-environment-variables DB_PASSWORD="$DB_PASSWORD" JWT_SECRET="$JWT_SECRET" \
  --restart-policy OnFailure
```

---

## Passo 8 — Deploy automatizado

Os passos 3, 5, 6 e 7 estão todos no [`deploy.sh`](deploy.sh). Para executar tudo de
uma vez:

```bash
chmod +x deploy.sh
sed -i 's/\r$//' deploy.sh
./deploy.sh
```

O script pergunta as credenciais na execução — nada fica gravado no repositório:

| Pergunta | Valor |
|---|---|
| Nome do banco de dados | *Enter* — usa `agrovision` |
| Usuário do banco | *Enter* — usa `agrouser` |
| Senha do usuário do banco | definida na execução |
| Senha do root do MySQL | definida na execução |
| Chave JWT (mín. 32 caracteres) | definida na execução |

Ao final ele imprime as URLs de acesso.

---

## Passo 9 — Verificar

Todos os recursos criados:

```bash
az resource list --resource-group rg-562766-cp4 --output table
```

Estado dos containers:

```bash
az container list --resource-group rg-562766-cp4 --output table
```

Log da aplicação — deve conter `Started AgroVisionApplication`:

```bash
az container logs --resource-group rg-562766-cp4 --name 562766-app
```

Endereço público da aplicação:

```bash
az container show \
  --resource-group rg-562766-cp4 \
  --name 562766-app \
  --query ipAddress.fqdn -o tsv
```

Comprovar que o container **não roda como root**:

```bash
az container exec \
  --resource-group rg-562766-cp4 \
  --name 562766-app \
  --exec-command "whoami"
```

Saída esperada: `appuser`.

---

## Passo 10 — Testar o CRUD na nuvem

Substitua `<APP_FQDN>` pelo endereço obtido no passo anterior. A porta **8080** é
obrigatória, e o protocolo é **http**.

Documentação interativa:

```
http://<APP_FQDN>:8080/swagger-ui.html
```

```bash
APP=http://<APP_FQDN>:8080

# CREATE — usuário (endpoint público)
curl -X POST $APP/api/usuarios \
  -H "Content-Type: application/json" \
  -d @testes/01_usuario_post.json

# Login — obter o token JWT
curl -X POST $APP/api/auth/login \
  -H "Content-Type: application/json" \
  -d @testes/02_auth_login.json
```

Guarde o token e use nas demais chamadas:

```bash
TOKEN="<token retornado no login>"

# CREATE — plantação
curl -X POST $APP/api/plantacoes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @testes/04_plantacao_post.json

# READ
curl $APP/api/plantacoes -H "Authorization: Bearer $TOKEN"

# UPDATE
curl -X PUT $APP/api/plantacoes/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @testes/05_plantacao_put.json

# DELETE
curl -X DELETE $APP/api/plantacoes/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Endpoints

| Método | Endpoint | Autenticação |
|---|---|---|
| `POST` | `/api/usuarios` | Público |
| `POST` | `/api/auth/login` | Público |
| `GET` `PUT` `DELETE` | `/api/usuarios/{id}` | JWT |
| `POST` `GET` `PUT` `DELETE` | `/api/plantacoes` | JWT |
| `POST` `GET` `PUT` `DELETE` | `/api/insumos` | JWT |
| `POST` `GET` `PUT` `DELETE` | `/api/safras` | JWT |
| `POST` `GET` `PUT` `DELETE` | `/api/relatorios` | JWT |
| `GET` | `/api/logs` | JWT |

---

## Passo 11 — Evidenciar a persistência por SELECT

Acessar o MySQL dentro do container em execução na Azure:

```bash
az container exec \
  --resource-group rg-562766-cp4 \
  --name 562766-db \
  --exec-command "/bin/bash"
```

Já dentro do container:

```bash
mysql -u agrouser -p agrovision
```

Consultas de evidência, executadas após cada operação do CRUD:

```sql
SHOW TABLES;

SELECT * FROM TB_USER_GS;
SELECT * FROM TB_PLANTACOES_GS;
SELECT * FROM TB_INSUMO_GS;
SELECT * FROM TB_SAFRA_GS;
SELECT * FROM TB_RELATORIO_GS;
SELECT * FROM TB_LOG_ERRO_GS;
```

### Comprovar o volume persistente

```bash
az container restart --resource-group rg-562766-cp4 --name 562766-db
```

Após o restart, repetir os `SELECT`: os registros continuam no banco, porque
`/var/lib/mysql` está montado no Azure File Share e não no sistema de arquivos
efêmero do container.

---

## Segurança

- O container da aplicação executa sob o usuário não privilegiado `appuser`, criado
  no `Dockerfile` — **nunca como root**. Comprovável por `whoami` (passo 9).
- Nenhuma credencial está gravada no código-fonte. Senhas e chave JWT são informadas
  na execução do `deploy.sh` e injetadas por `--secure-environment-variables`.
- A aplicação **não sobe** sem a variável `JWT_SECRET` — falha explícita em vez de
  usar uma chave padrão.
- O arquivo `.env` está no `.gitignore` e não é versionado.
- O banco é MySQL 8.0 em container — **não é H2**.

---

## Notas de execução no Windows

Três ajustes presentes no `deploy.sh`, todos comentados no próprio arquivo:

| Situação | Ajuste |
|---|---|
| A assinatura bloqueia `brazilsouth` por política | `LOCATION=eastus` |
| O Azure CLI no Git Bash devolve valores com `\r` | `tr -d '\r'` nas capturas |
| O Git Bash converte `/var/lib/mysql` em caminho do Windows | `export MSYS_NO_PATHCONV=1` |

O `az container create` não aceita `--tags`; as tags são aplicadas ao Resource Group,
ao ACR e à Conta de Armazenamento.

---

## Remover os recursos

```bash
az group delete --name rg-562766-cp4 --yes --no-wait
```
