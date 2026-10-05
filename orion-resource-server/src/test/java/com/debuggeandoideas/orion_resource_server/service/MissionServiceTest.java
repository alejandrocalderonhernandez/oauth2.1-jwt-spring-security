package com.debuggeandoideas.orion_resource_server.service;

import com.debuggeandoideas.orion_resource_server.dto.CreatedMissionResponse;
import com.debuggeandoideas.orion_resource_server.dto.CrewMemberResponse;
import com.debuggeandoideas.orion_resource_server.dto.MissionResponse;
import com.debuggeandoideas.orion_resource_server.dto.NewMissionRequest;
import com.debuggeandoideas.orion_resource_server.model.CrewMember;
import com.debuggeandoideas.orion_resource_server.model.Mission;
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
class MissionServiceTest {

    private static final CreatedMissionResponse EXPECTED_CREATED_MISSION = new CreatedMissionResponse(
            "M-005",
            "PENDIENTE_DE_REVISION",
            "Misión recibida por el centro de control. Un comité muy serio la revisará (mañana, quizá).");

    @Mock
    private MissionControlStore missionControlStore;

    @InjectMocks
    private MissionService missionService;

    @Test
    void mapsEveryMissionKeepingTheOrder() {
        when(missionControlStore.findAllMissions()).thenReturn(List.of(
                new Mission("M-001", "Luna Roja", "Fobos", "ACTIVA", "SECRETO", "2087-04-10", "Resumen uno",
                        List.of(new CrewMember("Ana Pérez", "Navegación")), List.of("Hallazgo uno"), "Nota uno"),
                new Mission("M-002", "Faro Oscuro", "Titán", "CONCLUIDA", "CONFIDENCIAL", "2087-08-20", "Resumen dos",
                        List.of(), List.of(), "Nota dos")));

        List<MissionResponse> responses = missionService.findAll();

        assertThat(responses).extracting(MissionResponse::id).containsExactly("M-001", "M-002");
        assertThat(responses.getFirst()).isEqualTo(new MissionResponse(
                "M-001", "Luna Roja", "Fobos", "ACTIVA", "SECRETO", "2087-04-10", "Resumen uno",
                List.of(new CrewMemberResponse("Ana Pérez", "Navegación")), List.of("Hallazgo uno"), "Nota uno"));
    }

    @Test
    void neverReturnsNullCrewOrFindings() {
        when(missionControlStore.findAllMissions()).thenReturn(List.of(
                new Mission("M-001", "Luna Roja", "Fobos", "ACTIVA", "SECRETO", "2087-04-10", "Resumen",
                        null, null, "Nota")));

        MissionResponse response = missionService.findAll().getFirst();

        assertThat(response.crew()).isNotNull().isEmpty();
        assertThat(response.findings()).isNotNull().isEmpty();
    }

    @Test
    void createReturnsTheFixedResponseWithARequest() {
        NewMissionRequest request = new NewMissionRequest("Aurora Verde", "Marte", "Explorar");

        assertThat(missionService.create(request)).isEqualTo(EXPECTED_CREATED_MISSION);
    }

    @Test
    void createReturnsTheFixedResponseWithoutARequest() {
        assertThat(missionService.create(null)).isEqualTo(EXPECTED_CREATED_MISSION);
    }
}
