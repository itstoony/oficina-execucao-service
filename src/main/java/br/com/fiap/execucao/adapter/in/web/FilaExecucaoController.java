package br.com.fiap.execucao.adapter.in.web;

import br.com.fiap.execucao.application.port.in.FilaExecucaoDTO;
import br.com.fiap.execucao.application.port.in.FilaExecucaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/execucao/fila")
@RequiredArgsConstructor
@Tag(name = "Fila de Execução", description = "Gestão da fila de execução de Ordens de Serviço")
@SecurityRequirement(name = "bearerAuth")
public class FilaExecucaoController {

    private final FilaExecucaoUseCase filaExecucaoUseCase;

    @GetMapping
    @Operation(summary = "Listar fila de execução ordenada por data de entrada")
    public List<FilaExecucaoDTO.Response> listarFila() {
        return filaExecucaoUseCase.listarFila();
    }

    @PostMapping("/{osId}/iniciar")
    @Operation(summary = "Iniciar execução de uma OS")
    public FilaExecucaoDTO.Response iniciarExecucao(@PathVariable UUID osId) {
        return filaExecucaoUseCase.iniciarExecucao(osId);
    }

    @PostMapping("/{osId}/finalizar")
    @Operation(summary = "Finalizar execução de uma OS")
    public FilaExecucaoDTO.Response finalizarExecucao(@PathVariable UUID osId) {
        return filaExecucaoUseCase.finalizarExecucao(osId);
    }
}
