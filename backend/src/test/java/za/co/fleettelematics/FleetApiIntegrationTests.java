package za.co.fleettelematics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class FleetApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void collectionEndpointsReturnJsonArrays() throws Exception {
        List<String> paths = List.of(
                "/api/v1/vehicles",
                "/api/v1/drivers",
                "/api/v1/driver-assignments",
                "/api/v1/telemetry/vehicle/" + firstId("SELECT vehicle_id FROM telemetry ORDER BY id LIMIT 1"),
                "/api/v1/gps-events",
                "/api/v1/gps-events/vehicle/" + firstId("SELECT vehicle_id FROM gps_events ORDER BY id LIMIT 1"),
                "/api/v1/trips",
                "/api/v1/trip-points/trip/" + firstId("SELECT trip_id FROM trip_points ORDER BY id LIMIT 1"),
                "/api/v1/alerts",
                "/api/v1/maintenance-records",
                "/api/v1/geofences",
                "/api/v1/audit-logs");
        for (String path : paths) {
            try {
                assertJsonArray(path);
            } catch (Exception | AssertionError exception) {
                throw new AssertionError("Collection endpoint failed: GET " + path, exception);
            }
        }
    }

    @Test
    void detailEndpointsReturnExistingRecords() throws Exception {
        String[][] endpoints = {
                {"SELECT id FROM vehicles ORDER BY id LIMIT 1", "/api/v1/vehicles/"},
                {"SELECT id FROM drivers ORDER BY id LIMIT 1", "/api/v1/drivers/"},
                {"SELECT id FROM driver_assignments ORDER BY id LIMIT 1", "/api/v1/driver-assignments/"},
                {"SELECT id FROM trips ORDER BY id LIMIT 1", "/api/v1/trips/"},
                {"SELECT id FROM alerts ORDER BY id LIMIT 1", "/api/v1/alerts/"},
                {"SELECT id FROM maintenance_records ORDER BY id LIMIT 1", "/api/v1/maintenance-records/"},
                {"SELECT id FROM geofences ORDER BY id LIMIT 1", "/api/v1/geofences/"}
        };
        for (String[] endpoint : endpoints) {
            String path = endpoint[1] + firstId(endpoint[0]);
            try {
                mockMvc.perform(asAdmin(get(path).accept(MediaType.APPLICATION_JSON)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.id").isNotEmpty());
            } catch (Exception | AssertionError exception) {
                throw new AssertionError("Detail endpoint failed: GET " + path, exception);
            }
        }
    }

    @Test
    void latestTelemetryEndpointReturnsARecordedSample() throws Exception {
        Long vehicleId = firstId("SELECT vehicle_id FROM telemetry ORDER BY id LIMIT 1");
        mockMvc.perform(asAdmin(get("/api/v1/telemetry/vehicle/{vehicleId}/latest", vehicleId)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.vehicleId").value(vehicleId.intValue()));
    }

    @Test
    void unknownVehicleReturnsStructuredNotFoundResponse() throws Exception {
        mockMvc.perform(asAdmin(get("/api/v1/vehicles/{id}", Long.MAX_VALUE)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void invalidVehicleRequestIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(asAdmin(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details.registrationNumber").exists());
    }

    private void assertJsonArray(String path) throws Exception {
        mockMvc.perform(asAdmin(get(path).accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
        return request.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Long firstId(String sql) {
        Long id = jdbcTemplate.queryForObject(sql, Long.class);
        assertNotNull(id, "Expected an existing seeded record for query: " + sql);
        return id;
    }
}
