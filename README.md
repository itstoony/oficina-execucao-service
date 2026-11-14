# oficina-execucao-service

**FIAP Pós-Tech Software Architecture — Tech Challenge Fase 4**

Microsserviço responsável pelo **estoque de peças** e pela **fila de execução** de Ordens de Serviço da Oficina Mecânica.

---

## Responsabilidades

- CRUD de peças com controle de estoque (reserva, baixa, entrada)
- Fila de execução de Ordens de Serviço (AGUARDANDO → EM_EXECUCAO → FINALIZADA)
- Participante do **Saga Pattern coreografado** via RabbitMQ

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.3 |
| PostgreSQL | 16 |
| RabbitMQ | 3 |
| Flyway | — |
| JaCoCo | 80% mínimo |

## Eventos RabbitMQ

**Exchange:** `oficina.events` (TopicExchange)

### Consome
| Routing Key | Ação |
|---|---|
| `os.aprovada` | Reserva estoque das peças da OS |
| `os.cancelada` | Libera reservas e cancela na fila |
| `pagamento.confirmado` | Coloca OS na fila de execução |

### Produz
| Routing Key | Quando |
|---|---|
| `estoque.reservado` | Reserva concluída com sucesso |
| `estoque.indisponivel` | Peça sem estoque suficiente (rollback saga) |
| `execucao.finalizada` | OS finalizada na execução |

## Como rodar localmente

```bash
# Subir todos os serviços (app + PostgreSQL + RabbitMQ)
docker compose up --build

# API disponível em:
# http://localhost:8081/swagger-ui.html
# RabbitMQ Management: http://localhost:15672 (guest/guest)
```

## Endpoints

### Peças (requerem JWT)
```
POST   /api/execucao/pecas
GET    /api/execucao/pecas
GET    /api/execucao/pecas/criticas
GET    /api/execucao/pecas/{id}
PUT    /api/execucao/pecas/{id}
DELETE /api/execucao/pecas/{id}
POST   /api/execucao/pecas/{id}/entrada
GET    /api/execucao/pecas/{id}/movimentacoes
```

### Fila de Execução (requerem JWT)
```
GET    /api/execucao/fila
POST   /api/execucao/fila/{osId}/iniciar
POST   /api/execucao/fila/{osId}/finalizar
```

## Testes

```bash
./mvnw test                    # Executar testes
./mvnw verify                  # Testes + relatório JaCoCo
# Relatório: target/site/jacoco/index.html
```

## CI/CD

Pipeline no GitHub Actions: **build → test → sonar → docker → deploy**

Secrets necessários: `DOCKER_USERNAME`, `DOCKER_PASSWORD`, `KUBECONFIG`, `SONAR_TOKEN`
