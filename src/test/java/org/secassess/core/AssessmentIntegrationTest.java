package org.secassess.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.secassess.core.dto.CopyCriteriaRequestDto;
import org.secassess.core.dto.CopyStatsResponseDto;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AssessmentIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private JwtService jwtService;
    @Autowired private AssessmentRepository assessmentRepository;
    @Autowired private AssessmentTemplateRepository templateRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private TemplateCriterionRepository criterionRepository;
    @Autowired private ProjectRepository projectRepository;

    private UUID assessmentId;
    private UUID templateId;
    private String orgSlug;

    @BeforeEach
    void setup() {
        criterionRepository.deleteAll();
        assessmentRepository.deleteAll();
        templateRepository.deleteAll();
        projectRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Test Org").slug("test-org").build());
        orgSlug = org.getSlug();

        Project prj = projectRepository.saveAndFlush(Project.builder()
                .name("P").code("C1").organization(org).build());

        AssessmentTemplate tmpl = templateRepository.saveAndFlush(AssessmentTemplate.builder()
                .organizationId(org.getId()).title("T").status(TemplateStatus.PUBLISHED).build());
        templateId = tmpl.getId();

        criterionRepository.saveAndFlush(TemplateCriterion.builder()
                .template(tmpl).section("S").text("T").severity(Severity.HIGH).weight(BigDecimal.ONE).build());

        Assessment ass = assessmentRepository.saveAndFlush(Assessment.builder()
                .projectId(prj.getId()).title("A").status(AssessmentStatus.OPEN).build());
        assessmentId = ass.getId();
    }

    @Test
    @DisplayName("1. Success: Copy criteria with valid token and valid data")
    void shouldCopySuccessfully() {
        String token = jwtService.generateToken("admin@test.com", "ROLE_ADMIN");

        CopyCriteriaRequestDto request = CopyCriteriaRequestDto.builder()
                .templateId(templateId)
                .sourceOrganizationSlug(orgSlug)
                .targetVersion("1.0.0")
                .build();

        ResponseEntity<CopyStatsResponseDto> response = callApi(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCopied()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. Validation Error: Invalid SemVer format should return 400")
    void shouldReturn400ForInvalidSemVer() {
        String token = jwtService.generateToken("admin@test.com", "ROLE_ADMIN");

        CopyCriteriaRequestDto request = CopyCriteriaRequestDto.builder()
                .templateId(templateId)
                .sourceOrganizationSlug(orgSlug)
                .targetVersion("invalid-version-text")
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/assessments/" + assessmentId + "/copy-from-template",
                HttpMethod.POST,
                new HttpEntity<>(request, createHeaders(token)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Semantic Versioning");
    }

    @Test
    @DisplayName("3. Security Error: ROLE_VIEWER should be forbidden from copying")
    void shouldReturn403ForUnauthorizedRole() {

        String token = jwtService.generateToken("viewer@test.com", "ROLE_VIEWER");

        CopyCriteriaRequestDto request = CopyCriteriaRequestDto.builder()
                .templateId(templateId)
                .sourceOrganizationSlug(orgSlug)
                .build();

        ResponseEntity<CopyStatsResponseDto> response = callApi(token, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("4. Auth Error: No token should return 401")
    void shouldReturn401ForNoToken() {
        CopyCriteriaRequestDto request = new CopyCriteriaRequestDto();

        ResponseEntity<CopyStatsResponseDto> response = restTemplate.postForEntity(
                "/api/v1/assessments/" + assessmentId + "/copy-from-template",
                request,
                CopyStatsResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // Helper Methods
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private ResponseEntity<CopyStatsResponseDto> callApi(String token, CopyCriteriaRequestDto request) {
        return restTemplate.exchange(
                "/api/v1/assessments/" + assessmentId + "/copy-from-template",
                HttpMethod.POST,
                new HttpEntity<>(request, createHeaders(token)),
                CopyStatsResponseDto.class
        );
    }
}