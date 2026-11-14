-- ============================================================
-- V1 — Schema do execucao-service
-- ============================================================

CREATE TABLE pecas (
    id              UUID           PRIMARY KEY,
    nome            VARCHAR(255)   NOT NULL,
    codigo          VARCHAR(255)   NOT NULL UNIQUE,
    preco_unitario  NUMERIC(10, 2) NOT NULL,
    qtd_estoque     INTEGER        NOT NULL,
    qtd_reservada   INTEGER        NOT NULL,
    qtd_minima      INTEGER        NOT NULL,
    criado_em       TIMESTAMP      NOT NULL,
    atualizado_em   TIMESTAMP      NOT NULL
);

CREATE TABLE movimentacoes_estoque (
    id                UUID         PRIMARY KEY,
    peca_id           UUID         NOT NULL REFERENCES pecas(id),
    tipo              VARCHAR(25)  NOT NULL,
    quantidade        INTEGER      NOT NULL,
    os_id             UUID,
    observacao        VARCHAR(500),
    data_movimentacao TIMESTAMP    NOT NULL
);

CREATE TABLE fila_execucao (
    id           UUID        PRIMARY KEY,
    os_id        UUID        NOT NULL UNIQUE,
    status       VARCHAR(20) NOT NULL,
    data_entrada TIMESTAMP   NOT NULL,
    data_inicio  TIMESTAMP,
    data_fim     TIMESTAMP
);

CREATE INDEX idx_pecas_codigo           ON pecas(codigo);
CREATE INDEX idx_movimentacoes_peca_id  ON movimentacoes_estoque(peca_id);
CREATE INDEX idx_fila_os_id             ON fila_execucao(os_id);
CREATE INDEX idx_fila_status            ON fila_execucao(status);
