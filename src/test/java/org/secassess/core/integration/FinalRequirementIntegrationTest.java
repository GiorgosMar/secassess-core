package org.secassess.core.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.secassess.core.dto.CopyCriteriaRequestDto;
import org.secassess.core.dto.CopyStatsResponseDto;
import org.secassess.core.dto.UpdateStatusRequestDto;
import org.secassess.core.enums.AssessmentStatus;
import org.secassess.core.enums.Severity;
import org.secassess.core.enums.TemplateStatus;
import org.secassess.core.interfaces.JwtService;
import org.secassess.core.model.*;
import org.secassess.core.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        // Use 'simple' cache to avoid Redis connection issues during local execution
        properties = "spring.cache.type=simple"
)
@ActiveProfiles("test")
class FinalRequirementIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JwtService jwtService;

    @Autowired private AssessmentRepository assessmentRepository;
    @Autowired private AssessmentTemplateRepository templateRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private TemplateCriterionRepository criterionRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private AssessmentItemRepository itemRepository;

    private UUID assessmentId;
    private UUID templateId;
    private String orgSlug;

    @BeforeEach
    void setup() {
        // Clear DB to ensure test isolation and prevent data pollution
        itemRepository.deleteAll();
        criterionRepository.deleteAll();
        assessmentRepository.deleteAll();
        templateRepository.deleteAll();
        projectRepository.deleteAll();
        organizationRepository.deleteAll();

        // Seed required data: Org -> Project -> Assessment
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Core Systems").slug("core-sys").build());
        orgSlug = org.getSlug();

        Project prj = projectRepository.saveAndFlush(Project.builder()
                .name("Project X").code("PX01").organization(org).build());

        Assessment ass = assessmentRepository.saveAndFlush(Assessment.builder()
                .projectId(prj.getId()).title("Security Audit 2024").status(AssessmentStatus.OPEN).build());
        assessmentId = ass.getId();

        // Create a PUBLISHED template as per Req 3 requirements
        AssessmentTemplate tmpl = templateRepository.saveAndFlush(AssessmentTemplate.builder()
                .organizationId(org.getId()).title("Standard Template")
                .status(TemplateStatus.PUBLISHED).build());
        templateId = tmpl.getId();

        // Use weight 1.0 to satisfy both DB precision (3,2) and check constraints
        criterionRepository.saveAndFlush(TemplateCriterion.builder()
                .template(tmpl).section("Authentication").text("MFA Enabled")
                .severity(Severity.HIGH).weight(BigDecimal.valueOf(1.0)).build());

        criterionRepository.saveAndFlush(TemplateCriterion.builder()
                .template(tmpl).section("Network").text("Port Scanning")
                .severity(Severity.MEDIUM).weight(BigDecimal.valueOf(1.0)).build());
    }

    @Test
    @DisplayName("Requirement 3: Copy Criteria Logic & Mapping Validation")
    void testCopyCriteriaFunctionality() {
        // Generate token for ASSESSOR role using the provided bypass method
        String token = jwtService.generateToken("assessor@test.com", "ROLE_ASSESSOR");

        CopyCriteriaRequestDto request = CopyCriteriaRequestDto.builder()
                .sourceOrganizationSlug(orgSlug)
                .templateId(templateId)
                .includeSections(List.of("Authentication")) // Test filtering logic
                .overwriteExisting(false)
                .build();

        // Perform real HTTP POST to verify API v1 versioning and JSON handling
        ResponseEntity<CopyStatsResponseDto> response = restTemplate.exchange(
                "/api/v1/assessments/" + assessmentId + "/copy-from-template",
                HttpMethod.POST,
                new HttpEntity<>(request, createHeaders(token)),
                CopyStatsResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCopied()).isEqualTo(1); // Confirm mapping works
    }

    @Test
    @DisplayName("Requirement 4: Status Transition - Block COMPLETED if scores are null")
    void testStatusTransitionValidation() {
        String token = jwtService.generateToken("assessor@test.com", "ROLE_ASSESSOR");

        // First copy items
        testCopyCriteriaFunctionality();

        UpdateStatusRequestDto statusRequest = new UpdateStatusRequestDto();
        statusRequest.setStatus(AssessmentStatus.COMPLETED);

        // Verify business rule: COMPLETED status requires all items to have scores
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/assessments/" + assessmentId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(statusRequest, createHeaders(token)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Requirement 5: RBAC - VIEWER must be FORBIDDEN from modifying data")
    void testViewerRolePermissions() {
        // Verify ROLE_VIEWER cannot access write-intent endpoints (403 Forbidden)
        String token = jwtService.generateToken("viewer@test.com", "ROLE_VIEWER");

        CopyCriteriaRequestDto request = CopyCriteriaRequestDto.builder()
                .templateId(templateId)
                .build();

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/v1/assessments/" + assessmentId + "/copy-from-template",
                HttpMethod.POST,
                new HttpEntity<>(request, createHeaders(token)),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Requirement 2: Pagination & Filtering Check")
    void testListEndpointsAndPagination() {
        String token = jwtService.generateToken("admin@test.com", "ROLE_ADMIN");

        // Validate that pagination (page/size) and filtering (status) parameters are processed
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/assessments?page=0&size=10&status=OPEN",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(token)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Security Audit 2024");
    }

    // Helper method to attach Bearer Token to outgoing test requests
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}