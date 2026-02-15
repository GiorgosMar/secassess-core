package org.secassess.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.secassess.core.interfaces.AssessmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable; // <--- ΑΥΤΟ ΕΛΕΙΠΕ
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.cache.type=simple")
@AutoConfigureMockMvc
public class AssessmentCacheTest {

    private static final Logger log = LoggerFactory.getLogger(AssessmentCacheTest.class);

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private AssessmentService assessmentService;

    @Test
    @DisplayName("Verify that second call to findAll is served from Cache")
    @WithMockUser(roles = "VIEWER") // Βεβαιώσου ότι ο ρόλος VIEWER υπάρχει στο σύστημά σου
    public void testFindAllIsCached() throws Exception {

        log.info("==========================================================");
        log.info("STEP 1: Executing first call (Expected: CACHE MISS)");
        log.info("==========================================================");

        // Πρώτη κλήση - Θα πρέπει να εκτελεστεί κανονικά
        mockMvc.perform(get("/api/v1/assessments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        log.info("==========================================================");
        log.info("STEP 2: Executing second call (Expected: CACHE HIT)");
        log.info("==========================================================");

        // Δεύτερη κλήση - Θα πρέπει να έρθει από την Cache
        mockMvc.perform(get("/api/v1/assessments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        log.info("==========================================================");
        log.info("STEP 3: Verifying that Service was invoked only ONCE");
        log.info("==========================================================");

        // Επαλήθευση ότι η μέθοδος του service κλήθηκε ΜΟΝΟ 1 φορά
        verify(assessmentService, times(1)).findAll(any(Pageable.class));

        log.info("SUCCESS: Cache logic is working perfectly!");
        log.info("==========================================================");
    }
}