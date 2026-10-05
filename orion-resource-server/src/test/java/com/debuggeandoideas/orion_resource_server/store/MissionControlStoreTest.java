package com.debuggeandoideas.orion_resource_server.store;

import com.debuggeandoideas.orion_resource_server.model.CrewMember;
import com.debuggeandoideas.orion_resource_server.model.Mission;
import com.debuggeandoideas.orion_resource_server.model.News;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MissionControlStoreTest {

    private MissionControlStore store;

    @BeforeEach
    void setUp() {
        store = new MissionControlStore();
        store.generateData();
    }

    @Test
    void generatesFiveNewsWithSortedIds() {
        assertThat(store.findAllNews())
                .extracting(News::id)
                .containsExactly("N-101", "N-102", "N-103", "N-104", "N-105");
    }

    @Test
    void generatesFourMissionsWithSortedIds() {
        assertThat(store.findAllMissions())
                .extracting(Mission::id)
                .containsExactly("M-001", "M-002", "M-003", "M-004");
    }

    @Test
    void everyMissionHasAnAllowedClassification() {
        assertThat(store.findAllMissions())
                .extracting(Mission::classification)
                .allMatch(classification -> List.of("ULTRA SECRETO", "SECRETO", "CONFIDENCIAL").contains(classification));
    }

    @Test
    void everyMissionHasValidCrewAndFindings() {
        for (Mission mission : store.findAllMissions()) {
            assertThat(mission.crew()).hasSizeBetween(2, 4);
            assertThat(mission.crew()).extracting(CrewMember::name).allMatch(name -> !name.isBlank());
            assertThat(mission.crew()).extracting(CrewMember::specialty).allMatch(specialty -> !specialty.isBlank());
            assertThat(mission.crew()).extracting(CrewMember::specialty).doesNotHaveDuplicates();
            assertThat(mission.findings()).hasSizeBetween(1, 3).doesNotHaveDuplicates();
        }
    }

    @Test
    void separateInstancesProduceIdenticalData() {
        MissionControlStore anotherStore = new MissionControlStore();
        anotherStore.generateData();

        assertThat(anotherStore.findAllNews()).isEqualTo(store.findAllNews());
        assertThat(anotherStore.findAllMissions()).isEqualTo(store.findAllMissions());
    }
}
