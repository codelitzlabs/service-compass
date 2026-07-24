package dev.codelitz.context.servicecatalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest @AutoConfigureMockMvc @Testcontainers(disabledWithoutDocker = true)
class ServiceCatalogIntegrationTest {
    @Container @ServiceConnection static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    @Autowired MockMvc mvc;
    @Test void createsAndFindsAService() throws Exception {
        mvc.perform(post("/api/teams").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Payments\",\"description\":\"Payments team\",\"owners\":[\"Ada\"]}"))
            .andExpect(status().isCreated());
        var body = "{\"name\":\"payments-api\",\"description\":\"Processes payments\",\"owners\":[\"Payments on-call\",\"Platform contact\"],\"teams\":[\"Payments\"],\"lifecycle\":\"production\",\"repositoryUrl\":\"https://github.com/example/payments-api\",\"tags\":[\"Java\",\"api\"],\"destinations\":[{\"name\":\"Metrics\",\"label\":\"metrics\",\"links\":[{\"url\":\"https://metrics.example.com/payments\",\"environment\":\"Production\"}]}]}";
        mvc.perform(post("/api/services").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("payments-api"))
            .andExpect(jsonPath("$.owners.length()").value(2))
            .andExpect(jsonPath("$.destinations[0].links[0].environment.name").value("Production"));
        mvc.perform(get("/api/services").param("query", "Payments")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/services/environments")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Production"));
    }
}
