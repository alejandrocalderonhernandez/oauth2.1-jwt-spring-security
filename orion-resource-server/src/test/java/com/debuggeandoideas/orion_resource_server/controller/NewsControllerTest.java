package com.debuggeandoideas.orion_resource_server.controller;

import com.debuggeandoideas.orion_resource_server.config.WebConfig;
import com.debuggeandoideas.orion_resource_server.dto.NewsResponse;
import com.debuggeandoideas.orion_resource_server.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsController.class)
@Import(WebConfig.class)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsService newsService;

    @Test
    void returnsNewsAsAJsonArrayAtTheRoot() throws Exception {
        when(newsService.findAll()).thenReturn(List.of(
                new NewsResponse("N-101", "2087-05-12", "CIENCIA", "Titular", "Resumen")));

        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("N-101"))
                .andExpect(jsonPath("$[0].date").value("2087-05-12"))
                .andExpect(jsonPath("$[0].tag").value("CIENCIA"))
                .andExpect(jsonPath("$[0].headline").value("Titular"))
                .andExpect(jsonPath("$[0].summary").value("Resumen"));
    }
}
