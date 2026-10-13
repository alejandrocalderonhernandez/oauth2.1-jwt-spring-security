package com.debuggeandoideas.orion_resource_server.controller;

import com.debuggeandoideas.orion_resource_server.config.SecurityConfig;
import com.debuggeandoideas.orion_resource_server.config.WebConfig;
import com.debuggeandoideas.orion_resource_server.dto.CreatedMissionResponse;
import com.debuggeandoideas.orion_resource_server.dto.CrewMemberResponse;
import com.debuggeandoideas.orion_resource_server.dto.MissionResponse;
import com.debuggeandoideas.orion_resource_server.service.MissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MissionController.class)
@Import({WebConfig.class, SecurityConfig.class})
class MissionControllerTest {

    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String SCOPE_MISSION_READ = "SCOPE_mission.read";
    private static final String SCOPE_MISSION_WRITE = "SCOPE_mission.write";
    private static final String API_MISSIONS = "/api/missions";


    private static final String CREATED_MESSAGE =
            "Misión recibida por el centro de control. Un comité muy serio la revisará (mañana, quizá).";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MissionService missionService;

    @BeforeEach
    void setUp() {
        when(missionService.create(any()))
                .thenReturn(new CreatedMissionResponse("M-005", "PENDIENTE_DE_REVISION", CREATED_MESSAGE));
    }

    @Test
    void returnsMissionsAsAJsonArrayWithCrewAndFindingsArrays() throws Exception {
        when(missionService.findAll()).thenReturn(List.of(new MissionResponse(
                "M-001", "Luna Roja", "Fobos", "ACTIVA", "ULTRA SECRETO", "2087-03-03", "Resumen",
                List.of(new CrewMemberResponse("Ana Pérez", "Navegación")), List.of("Hallazgo"), "Nota")));

        mockMvc.perform(get(API_MISSIONS).with(userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("M-001"))
                .andExpect(jsonPath("$[0].classification").value("ULTRA SECRETO"))
                .andExpect(jsonPath("$[0].crew").isArray())
                .andExpect(jsonPath("$[0].crew[0].name").value("Ana Pérez"))
                .andExpect(jsonPath("$[0].crew[0].specialty").value("Navegación"))
                .andExpect(jsonPath("$[0].findings").isArray())
                .andExpect(jsonPath("$[0].findings[0]").value("Hallazgo"));
    }

    @Test
    void createReturnsCreatedWithTheFixedBody() throws Exception {
        ResultActions result = mockMvc.perform(post(API_MISSIONS).with(managerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Aurora Verde\",\"destination\":\"Marte\",\"objective\":\"Explorar\"}"));

        expectFixedCreatedBody(result);
    }

    @Test
    void createReturnsCreatedWithAnEmptyBody() throws Exception {
        ResultActions result = mockMvc.perform(post(API_MISSIONS).with(managerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(""));

        expectFixedCreatedBody(result);
    }

    @Test
    void createReturnsCreatedWithoutAnyBody() throws Exception {
        expectFixedCreatedBody(mockMvc.perform(post(API_MISSIONS).with(managerToken())));
    }

    private RequestPostProcessor userToken() {
        return jwt().authorities(
                new SimpleGrantedAuthority(SCOPE_MISSION_READ),
                new SimpleGrantedAuthority(ROLE_USER)
        );
    }

    private RequestPostProcessor managerToken() {
        return jwt().authorities(
                new SimpleGrantedAuthority(SCOPE_MISSION_WRITE),
                new SimpleGrantedAuthority(ROLE_MANAGER)
        );
    }

    private void expectFixedCreatedBody(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("M-005"))
                .andExpect(jsonPath("$.status").value("PENDIENTE_DE_REVISION"))
                .andExpect(jsonPath("$.message").value(CREATED_MESSAGE));
    }
}
