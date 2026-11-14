package br.com.fiap.execucao.adapter.in.web;

import br.com.fiap.execucao.application.port.in.PecaDTO;
import br.com.fiap.execucao.application.port.in.PecaUseCase;
import br.com.fiap.execucao.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.execucao.shared.exception.RegraDeNegocioException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PecaController.class)
@Import({br.com.fiap.execucao.shared.exception.GlobalExceptionHandler.class,
         br.com.fiap.execucao.config.SecurityConfig.class,
         br.com.fiap.execucao.config.JwtAuthFilter.class,
         br.com.fiap.execucao.config.JwtService.class})
@ActiveProfiles("test")
class PecaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean PecaUseCase pecaUseCase;

    private PecaDTO.Response responsePadrao() {
        return new PecaDTO.Response(UUID.randomUUID(), "Filtro de óleo", "FO-001",
                new BigDecimal("25.00"), 10, 0, 10, 2, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void deveCadastrarPecaComSucesso() throws Exception {
        var request = new PecaDTO.CadastrarRequest("Filtro de óleo", "FO-001", new BigDecimal("25.00"), 10, 2);
        when(pecaUseCase.cadastrar(any())).thenReturn(responsePadrao());

        mockMvc.perform(post("/api/execucao/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Filtro de óleo"));
    }

    @Test
    @WithMockUser
    void deveListarPecas() throws Exception {
        when(pecaUseCase.listar()).thenReturn(List.of(responsePadrao()));

        mockMvc.perform(get("/api/execucao/pecas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("FO-001"));
    }

    @Test
    @WithMockUser
    void deveBuscarPecaPorId() throws Exception {
        UUID id = UUID.randomUUID();
        when(pecaUseCase.buscarPorId(id)).thenReturn(responsePadrao());

        mockMvc.perform(get("/api/execucao/pecas/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deveRetornar404QuandoPecaNaoEncontrada() throws Exception {
        UUID id = UUID.randomUUID();
        when(pecaUseCase.buscarPorId(id)).thenThrow(new RecursoNaoEncontradoException("Peça não encontrada"));

        mockMvc.perform(get("/api/execucao/pecas/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveRetornar422QuandoCodigoDuplicado() throws Exception {
        var request = new PecaDTO.CadastrarRequest("Filtro", "FO-001", new BigDecimal("25.00"), 5, 1);
        when(pecaUseCase.cadastrar(any())).thenThrow(new RegraDeNegocioException("Código já existe"));

        mockMvc.perform(post("/api/execucao/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser
    void deveExcluirPeca() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(pecaUseCase).excluir(id);

        mockMvc.perform(delete("/api/execucao/pecas/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/execucao/pecas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deveRegistrarEntradaDeEstoque() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new PecaDTO.EntradaRequest(5, "Reposição");
        when(pecaUseCase.registrarEntrada(eq(id), any())).thenReturn(responsePadrao());

        mockMvc.perform(post("/api/execucao/pecas/{id}/entrada", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deveListarPecasCriticas() throws Exception {
        when(pecaUseCase.buscarCriticas()).thenReturn(List.of(responsePadrao()));

        mockMvc.perform(get("/api/execucao/pecas/criticas"))
                .andExpect(status().isOk());
    }
}
