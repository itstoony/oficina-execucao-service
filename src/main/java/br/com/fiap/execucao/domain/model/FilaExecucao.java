package br.com.fiap.execucao.domain.model;

import br.com.fiap.execucao.domain.valueobject.StatusFila;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fila_execucao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilaExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "os_id", nullable = false, unique = true)
    private UUID osId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFila status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEntrada;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    @PrePersist
    private void prePersist() {
        dataEntrada = LocalDateTime.now();
    }
}
