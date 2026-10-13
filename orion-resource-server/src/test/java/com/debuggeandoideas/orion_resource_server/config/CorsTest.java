package com.debuggeandoideas.orion_resource_server.config;

import com.debuggeandoideas.orion_resource_server.controller.MissionController;
import com.debuggeandoideas.orion_resource_server.controller.NewsController;
import com.debuggeandoideas.orion_resource_server.service.MissionService;
import com.debuggeandoideas.orion_resource_server.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({NewsController.class, MissionController.class})
@Import({WebConfig.class, SecurityConfig.class})
class CorsTest {

    private static final String FRONTEND_ORIGIN = "http://localhost:5173";
    private static final String SCOPE_MISSION_READ = "SCOPE_mission.read";
    private static final String ROLE_USER = "ROLE_USER";
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsService newsService;

    @MockitoBean
    private MissionService missionService;

    @Test
    void preflightForPostFromTheFrontendIsAllowed() throws Exception {
        mockMvc.perform(options("/api/missions")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN));
    }

    @Test
    void getFromTheFrontendIncludesTheAllowOriginHeader() throws Exception {
        when(missionService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/missions").header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(SCOPE_MISSION_READ),
                                new SimpleGrantedAuthority(ROLE_USER)
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN));
    }
}
