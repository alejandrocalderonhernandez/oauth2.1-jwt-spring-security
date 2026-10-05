package com.debuggeandoideas.orion_resource_server.service;

import com.debuggeandoideas.orion_resource_server.dto.NewsResponse;
import com.debuggeandoideas.orion_resource_server.model.News;
import com.debuggeandoideas.orion_resource_server.store.MissionControlStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private MissionControlStore missionControlStore;

    @InjectMocks
    private NewsService newsService;

    @Test
    void mapsEveryNewsKeepingTheOrder() {
        when(missionControlStore.findAllNews()).thenReturn(List.of(
                new News("N-101", "2087-02-01", "CIENCIA", "Primer titular", "Primer resumen"),
                new News("N-102", "2087-03-01", "AVISO", "Segundo titular", "Segundo resumen")));

        List<NewsResponse> responses = newsService.findAll();

        assertThat(responses).containsExactly(
                new NewsResponse("N-101", "2087-02-01", "CIENCIA", "Primer titular", "Primer resumen"),
                new NewsResponse("N-102", "2087-03-01", "AVISO", "Segundo titular", "Segundo resumen"));
    }

    @Test
    void returnsAnEmptyListWhenThereIsNoNews() {
        when(missionControlStore.findAllNews()).thenReturn(List.of());

        assertThat(newsService.findAll()).isNotNull().isEmpty();
    }
}
