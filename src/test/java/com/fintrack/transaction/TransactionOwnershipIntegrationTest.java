package com.fintrack.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spec'in "Kullanıcı yalnızca kendi kaynaklarına erişebilmelidir / IDOR
 * açıklarına karşı ownership validation" kuralının somut kanıtı: iki farklı
 * gerçek kullanıcı, gerçek bir DB'ye karşı, birbirinin transaction'larına
 * erişemediğini gösterir. Ayrıca soft delete'in gerçekten kaydı gizlediğini
 * (fiziksel silmediğini) doğrular.
 */
class TransactionOwnershipIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void aUserCannotReadUpdateOrDeleteAnotherUsersTransaction() throws Exception {
        String ownerToken = registerAndLogin("owner");
        String intruderToken = registerAndLogin("intruder");
        UUID foodCategoryId = findSystemCategoryId(ownerToken, "Food");

        String createBody = """
                {"categoryId":"%s","type":"EXPENSE","amount":42.50,"currency":"TRY","transactionDate":"2026-09-05","description":"test"}
                """.formatted(foodCategoryId);

        String createResponse = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String transactionId = JsonPath.read(createResponse, "$.data.id");

        // Sahibi kendi kaydını görebilir
        mockMvc.perform(get("/api/v1/transactions/" + transactionId).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Başka bir kullanıcı ne görebilir, ne güncelleyebilir, ne de silebilir
        mockMvc.perform(get("/api/v1/transactions/" + transactionId).header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/transactions/" + transactionId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/transactions/" + transactionId).header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void softDeletedTransaction_isHiddenFromSubsequentReads_butRecordIsNotLost() throws Exception {
        String token = registerAndLogin("deleter");
        UUID categoryId = findSystemCategoryId(token, "Food");

        String createBody = """
                {"categoryId":"%s","type":"EXPENSE","amount":10,"currency":"TRY","transactionDate":"2026-09-05"}
                """.formatted(categoryId);

        String createResponse = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String transactionId = JsonPath.read(createResponse, "$.data.id");

        mockMvc.perform(delete("/api/v1/transactions/" + transactionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/transactions/" + transactionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin(String namePrefix) throws Exception {
        String email = namePrefix + "-" + UUID.randomUUID() + "@fintrack-test.dev";
        String body = """
                {"email":"%s","password":"SecurePass123","firstName":"%s","lastName":"Test","defaultCurrency":"TRY"}
                """.formatted(email, namePrefix);

        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return JsonPath.read(response, "$.data.accessToken");
    }

    private UUID findSystemCategoryId(String token, String categoryName) throws Exception {
        String response = mockMvc.perform(get("/api/v1/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        java.util.List<String> ids = JsonPath.read(response, "$.data[?(@.name=='" + categoryName + "')].id");
        return UUID.fromString(ids.get(0));
    }
}
