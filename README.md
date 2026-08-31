# AgroVision — Containers em Nuvem (ACR / ACI)

**FIAP — Tecnologia em Desenvolvimento de Sistemas**
Disciplina: DevOps Tools & Cloud Computing — 1º Checkpoint, 2º Semestre
Projeto DimDim

---

## Grupo CloudOps

| RM | Nome completo |
|---|---|
| 562074 | João Victor Caetano Alves da Silva *(representante)* |
| 564115 | João Victor Bueno Castelini da Silva |
| 565667 | Ryan Vetoriano |
| 562766 | Felipe Furlanetto |
| 564002 | Raul Rezende Iemini Aguiar |

---

## Sobre a solução

API REST de monitoramento agrícola containerizada e executada na Azure como duas
instâncias de container independentes:

- **Aplicação** — Java 21 / Spring Boot 3.3, autenticação JWT, documentação Swagger
- **Banco de dados** — MySQL 8.0 em container, com os dados persistidos em um
  Azure File Share vinculado a uma Conta de Armazenamento

Ambas as imagens são publicadas no **Azure Container Registry (ACR)** e executadas
em **Azure Container Instances (ACI)**. Todos os recursos são criados via **Azure CLI**
pelo script [`deploy.sh`](deploy.sh).

### Arquitetura

```
   Desenvolvedor                          Azure  (562074-grupo-rg)
   -------------                          ----------------------------------
   docker build      ──push──►   ACR  acr562074agrovision
        │                          │
        │                          │ pull
        ▼                          ▼
   docker compose            ┌──────────────┐        ┌──────────────┐
   (validação local)         │  ACI  562074-app  │──►│  ACI 562074-db  │
                             │  Spring Boot :8080│   │  MySQL 8.0 :3306│
                             └──────────────┘        └───────┬──────┘
                                     │                       │ volume
                                     │ IP público            ▼
                                  Internet          Storage Account
                                                    st562074agrovision
                                                    (file share: dbdata)
```

### Tabelas

| Tabela | Descrição |
|---|---|
| `TB_USER_GS` | Usuários (produtores rurais) |
| `TB_PLANTACOES_GS` | Plantações monitoradas |
| `TB_INSUMO_GS` | Insumos por plantação |
| `TB_SAFRA_GS` | Safras colhidas |
| `TB_RELATORIO_GS` | Relatórios gerados |
| `TB_LOG_ERRO_GS` | Log de erros da aplicação |

O script DDL completo está em [`db/init.sql`](db/init.sql) e é executado
automaticamente na primeira inicialização do container do banco.

---

## Estrutura do repositório

```
agrovision-gs/
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

## Pré-requisitos

- Docker e Docker Compose
- Azure CLI autenticado (`az login`)
- JDK 21 (opcional — o build acontece dentro do container)

## Passo 0 — Ambiente local

As credenciais nunca ficam gravadas no código. Para o ambiente local, o Docker
Compose lê um arquivo `.env` que **não é versionado**:

```bash
cp .env.example .env
```

O `deploy.sh` não usa esse arquivo — ele pergunta as credenciais na execução.

---

## Passo 1 — Build das imagens

```bash
# Imagem da aplicação
docker build -t 562074-app:latest .

# Imagem do banco de dados
docker build -t 562074-db:latest ./db
```

Conferir:

```bash
docker images | grep 562074
```

---

## Passo 2 — Executar e testar localmente

```bash
docker compose up -d --build
docker compose ps
```

Acompanhar a subida da aplicação:

```bash
docker compose logs -f app
```

A API fica em `http://localhost:8080` e o Swagger em
`http://localhost:8080/swagger-ui.html`.

Validar o CRUD antes de subir para a nuvem:

```bash
# 1. Criar usuário (endpoint público)
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d @testes/01_usuario_post.json

# 2. Autenticar e obter o token JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d @testes/02_auth_login.json
```

Encerrar o ambiente local:

```bash
docker compose down
```

---

## Passo 3 — Login no Azure Container Registry

```bash
az acr login --name acr562074agrovision

# alternativa equivalente
docker login acr562074agrovision.azurecr.io
```

---

## Passo 4 — Push das imagens para o ACR

```bash
docker tag 562074-app:latest acr562074agrovision.azurecr.io/562074-app:latest
docker tag 562074-db:latest  acr562074agrovision.azurecr.io/562074-db:latest

docker push acr562074agrovision.azurecr.io/562074-app:latest
docker push acr562074agrovision.azurecr.io/562074-db:latest
```

Conferir as imagens registradas:

```bash
az acr repository list --name acr562074agrovision --output table
```

---

## Passo 5 — Deploy completo na Azure

O script cria **todos** os recursos (Resource Group, ACR, Conta de Armazenamento,
File Share e os dois ACIs) e já executa os passos 1, 3 e 4. Ele pergunta as
credenciais na execução, então nenhuma senha fica no repositório.

```bash
az login

chmod +x deploy.sh
sed -i 's/\r$//' deploy.sh
./deploy.sh
```

No Windows, execute pelo **Git Bash**.

---

## Passo 6 — Verificar os recursos criados

```bash
# Todos os recursos do grupo
az resource list --resource-group 562074-grupo-rg --output table

# Containers em execução
az container list --resource-group 562074-grupo-rg --output table

# URL pública da aplicação
az container show --resource-group 562074-grupo-rg \
  --name 562074-app --query ipAddress.fqdn -o tsv
```

Logs da aplicação na nuvem:

```bash
az container logs --resource-group 562074-grupo-rg --name 562074-app
```

---

## Passo 7 — Testar o CRUD na nuvem

Substitua `<APP_FQDN>` pelo endereço retornado no passo anterior.

```bash
APP=http://<APP_FQDN>:8080

# CREATE — usuário
curl -X POST $APP/api/usuarios \
  -H "Content-Type: application/json" \
  -d @testes/01_usuario_post.json

# Login — obter o token JWT
TOKEN=$(curl -s -X POST $APP/api/auth/login \
  -H "Content-Type: application/json" \
  -d @testes/02_auth_login.json | jq -r '.token')

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

---

## Passo 8 — Evidenciar a persistência por SELECT

Acessar o MySQL dentro do container na Azure:

```bash
az container exec \
  --resource-group 562074-grupo-rg \
  --name 562074-db \
  --exec-command "/bin/bash"
```

Já dentro do container:

```bash
mysql -u agrouser -p agrovision
```

Consultas de evidência, executadas após cada operação do CRUD:

```sql
SELECT * FROM TB_USER_GS;
SELECT * FROM TB_PLANTACOES_GS;
SELECT * FROM TB_INSUMO_GS;
SELECT * FROM TB_SAFRA_GS;
SELECT * FROM TB_RELATORIO_GS;
SELECT * FROM TB_LOG_ERRO_GS;
```

### Comprovar o volume persistente

```bash
az container restart --resource-group 562074-grupo-rg --name 562074-db
```

Após o restart, repetir os `SELECT` — os registros continuam no banco, pois os
dados residem no Azure File Share e não no sistema de arquivos efêmero do container.

---

## Endpoints da API

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

Documentação interativa: `http://<APP_FQDN>:8080/swagger-ui.html`

---

## Segurança

- O container da aplicação executa sob o usuário não privilegiado `appuser`,
  criado no `Dockerfile` — **nunca como root**.
- Nenhuma credencial (usuário, senha, token ou chave JWT) está gravada no
  código-fonte. Tudo é injetado por variável de ambiente no deploy, usando
  `--secure-environment-variables` no Azure CLI para os valores sensíveis.
- O arquivo `.env` está no `.gitignore` e não é versionado.
- O banco de dados **não é H2** — é MySQL 8.0 em container.

---

## Limpeza dos recursos

```bash
az group delete --name 562074-grupo-rg --yes --no-wait
```
