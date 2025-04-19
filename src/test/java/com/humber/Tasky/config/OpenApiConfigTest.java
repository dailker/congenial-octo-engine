package com.humber.Tasky.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenUnauthenticatedUser_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/v3/api-docs"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedUser_thenAccessGranted() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")
               .with(SecurityMockMvcRequestPostProcessors.csrf()))
               .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs")
               .with(SecurityMockMvcRequestPostProcessors.csrf()))
               .andExpect(status().isOk());
    }
}