package com.berkay.todo;

import com.berkay.todo.dto.request.LoginRequest;
import com.berkay.todo.dto.request.RegisterRequest;
import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.repository.TaskRepository;
import com.berkay.todo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        // İlişkili tablolar olduğu için önce task'leri, sonra user'ları temizliyoruz
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Gerçek HTTP POST -> /auth/register ile kullanıcı kaydı
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("berkay@example.com");
        registerRequest.setPassword("123456");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Gerçek HTTP POST -> /auth/login ile giriş yap ve gerçek JWT token al
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("berkay@example.com");
        loginRequest.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        this.jwtToken = responseJson.get("token").asText();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Güvenlik: Token olmadan istek atıldığında 403 Forbidden dönmeli")
    void createTask_withoutToken_shouldReturnForbidden() throws Exception {
        TaskRequest request = new TaskRequest("Yetkisiz Görev", "Açıklama", false);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tam Akış: Geçerli JWT ile görev oluşturma (HTTP -> Filter -> Controller -> Service -> DB)")
    void createTask_withValidJwt_shouldPersistTaskInPostgresAndReturnCreated() throws Exception {
        TaskRequest request = new TaskRequest("PostgreSQL Entegrasyon Görevi", "Açıklama", false);

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("PostgreSQL Entegrasyon Görevi"))
                .andExpect(jsonPath("$.description").value("Açıklama"))
                .andExpect(jsonPath("$.completed").value(false));

        // Gerçek PostgreSQL doğrulaması
        assertEquals(1, taskRepository.count());
        var savedTask = taskRepository.findAll().get(0);
        assertEquals("PostgreSQL Entegrasyon Görevi", savedTask.getTitle());
        assertEquals("berkay@example.com", savedTask.getUser().getEmail());
    }

    @Test
    @DisplayName("Tam Akış: Görevleri listeleme (Sadece giriş yapan kullanıcının görevleri listelenmeli)")
    void getTasks_shouldReturnUserTasks() throws Exception {
        // Önce 1 görev oluşturalım
        TaskRequest request = new TaskRequest("Listeleme Görevi", "Açıklama", false);
        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // GET /tasks isteği atalım
        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Listeleme Görevi"));
    }

    @Test
    @DisplayName("Tam Akış: Görevi güncelleme ve ardından silme akışı")
    void updateAndDeleteTask_shouldWorkEndToEnd() throws Exception {
        // 1. Görev oluştur
        TaskRequest createRequest = new TaskRequest("İlk Başlık", "İlk Açıklama", false);
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Görevi güncelle (PUT /tasks/{id})
        TaskRequest updateRequest = new TaskRequest("Güncellenmiş Başlık", "Yeni Açıklama", true);
        mockMvc.perform(put("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Güncellenmiş Başlık"))
                .andExpect(jsonPath("$.completed").value(true));

        // 3. Görevi sil (DELETE /tasks/{id})
        mockMvc.perform(delete("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Veritabanında görev kalmadığını doğrula
        assertEquals(0, taskRepository.count());
    }
}
