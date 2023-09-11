package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void addUser_EmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        this.mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addUser_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        String invalidUser = "{" +
                "\"email\": \"invalidemail\", " +
                "\"login\": \"testlogin\", " +
                "\"name\": \"Test User\", " +
                "\"birthday\": \"1990-01-01\"" +
                "}";
        this.mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(invalidUser))
                .andExpect(status().isBadRequest());
    }

}
