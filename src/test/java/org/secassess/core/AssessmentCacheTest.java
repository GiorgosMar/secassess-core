package org.secassess.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.secassess.core.interfaces.AssessmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.data.domain.Pageable;
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

    @MockitoSpyBean
    private AssessmentService assessmentService;

    @Test
    @DisplayName("Verify that second call to findAll is served from Cache")
    @WithMockUser(roles = "VIEWER")
    public void testFindAllIsCached() throws Exception {

        log.info("==========================================================");
        log.info("STEP 1: Executing first call (Expected: CACHE MISS)");
        log.info("==========================================================");

        mockMvc.perform(get("/api/v1/assessments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        log.info("==========================================================");
        log.info("STEP 2: Executing second call (Expected: CACHE HIT - Service should NOT be called)");
        log.info("==========================================================");

        mockMvc.perform(get("/api/v1/assessments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        log.info("==========================================================");
        log.info("STEP 3: Verifying that Service was invoked only ONCE");
        log.info("==========================================================");

        verify(assessmentService, times(1)).findAll(any(Pageable.class));

        log.info("SUCCESS: Cache logic is working perfectly!");
        log.info("==========================================================");
    }
}