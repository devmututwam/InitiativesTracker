package zm.co.zanaco.tracker.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import zm.co.zanaco.tracker.dto.*;
import zm.co.zanaco.tracker.service.BudgetService;
import zm.co.zanaco.tracker.service.CostService;
import zm.co.zanaco.tracker.service.InitiativeService;
import zm.co.zanaco.tracker.service.ReportService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the Spring Security role matrix without requiring a live database.
 *
 * {@code @WithMockUser} injects a synthetic principal directly into the
 * {@link org.springframework.security.core.context.SecurityContext}, bypassing
 * the JWT filter. This lets each test focus solely on whether the authorisation
 * rule for a given role + endpoint is correct.
 *
 * Services are mocked so controllers never reach the JPA layer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:sectest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "jwt.secret=test-secret-key-that-is-at-least-32-characters",
        "jwt.expiration-ms=86400000",
        "server.servlet.context-path=",
        "app.seeder.enabled=false"
})
@DisplayName("Security rules")
class SecurityRulesTest {

    @Autowired private WebApplicationContext context;

    @MockitoBean private InitiativeService initiativeService;
    @MockitoBean private BudgetService budgetService;
    @MockitoBean private CostService costService;
    @MockitoBean private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Stub service defaults so controllers return 200/201 for authorised requests
        when(initiativeService.listInitiatives(any(), any())).thenReturn(Page.empty());
        when(initiativeService.getInitiative(anyLong()))
                .thenReturn(new InitiativeResponseDto(null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null));
    }

    // =========================================================================
    // Unauthenticated
    // =========================================================================

    @Nested
    @DisplayName("Unauthenticated requests")
    class Unauthenticated {

        @Test
        @DisplayName("GET /api/initiatives returns 401")
        void get_initiatives_noToken_401() throws Exception {
            mockMvc.perform(get("/api/initiatives"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/initiatives returns 401")
        void post_initiative_noToken_401() throws Exception {
            mockMvc.perform(post("/api/initiatives")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("/auth/login is public")
        void authLogin_isPublic() throws Exception {
            // Returns 400 (bad request – missing body) not 401
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // VIEWER
    // =========================================================================

    @Nested
    @DisplayName("VIEWER role")
    class ViewerRole {

        @Test
        @DisplayName("can GET /api/initiatives")
        @WithMockUser(roles = "VIEWER")
        void viewer_canGet_initiatives() throws Exception {
            mockMvc.perform(get("/api/initiatives"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("can GET /api/initiatives/{id}")
        @WithMockUser(roles = "VIEWER")
        void viewer_canGet_initiativeById() throws Exception {
            mockMvc.perform(get("/api/initiatives/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("cannot POST /api/initiatives → 403")
        @WithMockUser(roles = "VIEWER")
        void viewer_cannotCreate_initiative() throws Exception {
            mockMvc.perform(post("/api/initiatives")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot PATCH /api/initiatives/1 → 403")
        @WithMockUser(roles = "VIEWER")
        void viewer_cannotUpdate_initiative() throws Exception {
            mockMvc.perform(patch("/api/initiatives/1")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot POST /api/initiatives/1/status → 403")
        @WithMockUser(roles = "VIEWER")
        void viewer_cannotChange_status() throws Exception {
            mockMvc.perform(post("/api/initiatives/1/status")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot POST /api/initiatives/1/costs → 403")
        @WithMockUser(roles = "VIEWER")
        void viewer_cannotAdd_cost() throws Exception {
            mockMvc.perform(post("/api/initiatives/1/costs")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // DEVELOPER
    // =========================================================================

    @Nested
    @DisplayName("DEVELOPER role")
    class DeveloperRole {

        @Test
        @DisplayName("can GET /api/initiatives")
        @WithMockUser(roles = "DEVELOPER")
        void developer_canGet_initiatives() throws Exception {
            mockMvc.perform(get("/api/initiatives"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("can POST /api/initiatives/1/status (not 403)")
        @WithMockUser(roles = "DEVELOPER")
        void developer_canChange_status() throws Exception {
            // Service not stubbed → may return 4xx but NOT 403
            mockMvc.perform(post("/api/initiatives/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"newStatus\":\"IN_PROGRESS\"}"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("can POST /api/initiatives/1/costs (not 403)")
        @WithMockUser(roles = "DEVELOPER")
        void developer_canAdd_cost() throws Exception {
            mockMvc.perform(post("/api/initiatives/1/costs")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("cannot POST /api/initiatives → 403")
        @WithMockUser(roles = "DEVELOPER")
        void developer_cannotCreate_initiative() throws Exception {
            mockMvc.perform(post("/api/initiatives")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("cannot POST /api/initiatives/1/budgets → 403")
        @WithMockUser(roles = "DEVELOPER")
        void developer_cannotAttach_budget() throws Exception {
            mockMvc.perform(post("/api/initiatives/1/budgets")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // MANAGER
    // =========================================================================

    @Nested
    @DisplayName("MANAGER role")
    class ManagerRole {

        @Test
        @DisplayName("can POST /api/initiatives (create) – not 403")
        @WithMockUser(roles = "MANAGER")
        void manager_canCreate_initiative() throws Exception {
            mockMvc.perform(post("/api/initiatives")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("can PATCH /api/initiatives/1 – not 403")
        @WithMockUser(roles = "MANAGER")
        void manager_canUpdate_initiative() throws Exception {
            mockMvc.perform(patch("/api/initiatives/1")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("can POST /api/initiatives/1/budgets – not 403")
        @WithMockUser(roles = "MANAGER")
        void manager_canAttach_budget() throws Exception {
            mockMvc.perform(post("/api/initiatives/1/budgets")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("can DELETE /api/budgets/1 – not 403")
        @WithMockUser(roles = "MANAGER")
        void manager_canDelete_budget() throws Exception {
            mockMvc.perform(delete("/api/budgets/1"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }
    }

    // =========================================================================
    // ADMIN
    // =========================================================================

    @Nested
    @DisplayName("ADMIN role")
    class AdminRole {

        @Test
        @DisplayName("can POST /api/initiatives – not 403")
        @WithMockUser(roles = "ADMIN")
        void admin_canCreate_initiative() throws Exception {
            mockMvc.perform(post("/api/initiatives")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }

        @Test
        @DisplayName("can DELETE /api/costs/1 – not 403")
        @WithMockUser(roles = "ADMIN")
        void admin_canDelete_cost() throws Exception {
            mockMvc.perform(delete("/api/costs/1"))
                    .andExpect(result ->
                            assertNotForbidden(result.getResponse().getStatus()));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /**
     * Asserts the HTTP status is not 403 (Forbidden).
     * Security tests only verify the authorization decision; the actual
     * business response (200, 400, 404…) is irrelevant here.
     */
    private static void assertNotForbidden(int status) {
        if (status == 403) {
            throw new AssertionError("Expected NOT 403 Forbidden, but got 403");
        }
    }
}
