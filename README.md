# oficina-execucao-service

**FIAP Pós-Tech Software Architecture — Tech Challenge Fase 4**

Microsserviço responsável pelo **estoque de peças** e pela **fila de execução** de Ordens de Serviço da Oficina Mecânica.

---

## Arquitetura

```
┌──────────────────────────────────────────────────────┐
│              execucao-service (:8081)                │
│                                                      │
│  adapter/in/web  →  application/service  →  domain  │
│       ↕                    ↕                         │
│  adapter/out/persistence  adapter/out/messaging      │
│       ↕                    ↕                         │
│    PostgreSQL           RabbitMQ                     │
└──────────────────────────────────────────────────────┘
```

**Padrão:** Arquitetura Hexagonal (Ports & Adapters) + DDD

**Banco:** PostgreSQL (relacional — controle de estoque exige consistência transacional)

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.3 |
| PostgreSQL | 16 |
| RabbitMQ | 3 |
| Flyway | — |
| JaCoCo | 80% mínimo |

---

## Saga Pattern — Participante

Este serviço é **participante** do Saga Pattern coreografado. Reage ao evento `os.aprovada` reservando o estoque e publicando o resultado. Em caso de estoque insuficiente, publica `estoque.indisponivel` disparando o rollback da saga.

### Papel no fluxo

```
OS Service publica os.aprovada
  → Execucao reserva estoque das peças da OS
  → SE ok:    publica estoque.reservado  (saga avança)
  → SE falha: publica estoque.indisponivel (rollback)

Billing publica pagamento.confirmado
  → Execucao coloca OS na fila de execução

Admin chama POST /api/execucao/fila/{osId}/iniciar
  → Status fila: EM_EXECUCAO

Admin chama POST /api/execucao/fila/{osId}/finalizar
  → Baixa estoque reservado
  → publica: execucao.finalizada

OS Service publica os.cancelada
  → Execucao libera as reservas de estoque
```

---

## Endpoints

### Peças (requerem JWT)
| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/execucao/pecas` | Cadastrar peça |
| GET | `/api/execucao/pecas` | Listar peças |
| GET | `/api/execucao/pecas/criticas` | Peças com estoque crítico |
| GET | `/api/execucao/pecas/{id}` | Buscar peça |
| PUT | `/api/execucao/pecas/{id}` | Atualizar peça |
| DELETE | `/api/execucao/pecas/{id}` | Remover peça |
| POST | `/api/execucao/pecas/{id}/entrada` | Entrada de estoque |
| GET | `/api/execucao/pecas/{id}/movimentacoes` | Histórico de movimentações |

### Fila de Execução (requerem JWT)
| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/execucao/fila` | Listar fila ordenada |
| POST | `/api/execucao/fila/{osId}/iniciar` | Iniciar execução da OS |
| POST | `/api/execucao/fila/{osId}/finalizar` | Finalizar execução da OS |

---

## Eventos RabbitMQ

**Exchange:** `oficina.events` (TopicExchange)

### Publica
| Routing Key | Quando |
|-------------|--------|
| `estoque.reservado` | Reserva de estoque concluída com sucesso |
| `estoque.indisponivel` | Peça sem estoque suficiente (rollback saga) |
| `execucao.finalizada` | OS finalizada na execução |

### Consome
| Routing Key | Ação |
|-------------|------|
| `os.aprovada` | Reserva estoque das peças da OS |
| `os.cancelada` | Libera reservas e remove da fila |
| `pagamento.confirmado` | Coloca OS na fila de execução |

---

## Testes

### Unitários (JUnit 5 + Mockito)
```bash
./mvnw test
```

### Cobertura JaCoCo (mínimo 80%)
```bash
./mvnw verify
# Relatório: target/site/jacoco/index.html
```

> **Evidência de cobertura:** adicione aqui o print do relatório JaCoCo após rodar `./mvnw verify`

---

## Executar localmente

```bash
# Subir PostgreSQL + RabbitMQ
docker compose up -d

# Rodar aplicação
./mvnw spring-boot:run

# Swagger UI
http://localhost:8081/swagger-ui.html
# RabbitMQ Management: http://localhost:15672 (guest/guest)
```

---

## CI/CD

Pipeline: **build-test → sonar → docker → deploy**

O job `docker` só executa se `build-test` **e** `sonar` passarem.

Secrets necessários: `DOCKER_USERNAME`, `DOCKER_PASSWORD`, `KUBECONFIG`, `SONAR_TOKEN`

---

## Infraestrutura Kubernetes

```
k8s/
├── namespace.yaml   — namespace oficina
├── secret.yaml      — credenciais (DB, JWT, RabbitMQ)
├── configmap.yaml   — variáveis de ambiente
├── deployment.yaml  — app + PostgreSQL
└── hpa.yaml         — HorizontalPodAutoscaler (2–10 réplicas)
```
