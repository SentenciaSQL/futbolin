package com.futbolin.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbolin.api.dto.LoginRequest;
import com.futbolin.api.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void registerLoginAndFetchProfile() throws Exception {
        RegisterRequest register = new RegisterRequest(
                "andres@futbolin.app", "andresfc", "Password1!", "AndresFC", "BO", "Bolívar"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("andres@futbolin.app", "Password1!"))))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andresfc"))
                .andExpect(jsonPath("$.division").value("AMATEUR"))
                .andExpect(jsonPath("$.rankingPoints").value(1000));
    }

    @Test
    void socialLoginAcceptsUnsignedJwtInDev() throws Exception {
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"google-sub-1\",\"email\":\"social@futbolin.app\",\"name\":\"Social\"}".getBytes());
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes());
        String idToken = header + "." + payload + ".";
        mockMvc.perform(post("/api/v1/auth/social")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"google\",\"idToken\":\"" + idToken + "\",\"username\":\"socialfc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("socialfc"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        RegisterRequest register = new RegisterRequest(
                "dup@futbolin.app", "dupuser", "Password1!", "Dup", "AR", "Boca"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }
}
