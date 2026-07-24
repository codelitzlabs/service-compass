package dev.codelitz.context.servicecatalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LocalServiceCatalogIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void storesMultipleOwnersAndEnvironmentSpecificDestinations() throws Exception {
        mvc.perform(post("/api/teams").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"Checkout","description":"Owns checkout","owners":["Grace","Ada"]}
            """)).andExpect(status().isCreated()).andExpect(jsonPath("$.owners.length()").value(2));
        var body = """
            {
              "name": "checkout-api",
              "description": "Coordinates checkout",
              "owners": ["Checkout on-call", "Product engineering"],
              "teams": ["Checkout"],
              "lifecycle": "production",
              "repositoryUrl": "https://github.com/codelitz/checkout",
              "tags": ["java", "critical"],
              "destinations": [
                {"name":"Metrics", "label":"metrics", "links":[
                  {
                    "url":"https://metrics.example.com/checkout",
                    "environment":"Production",
                    "authenticationMethod":"SSO",
                    "accounts":[
                      {"label":"Administrator","identifier":"admin@example.com","authenticationMethod":"SSO"},
                      {"label":"Marketing","identifier":"marketing@example.com","authenticationMethod":"Sign in"}
                    ],
                    "accessNotes":"VPN required outside the office",
                    "accessUrl":"https://access.example.com/metrics"
                  },
                  {"url":"https://metrics.example.com/checkout-dev", "environment":"Development"}
                ]}
              ]
            }
            """;

        mvc.perform(post("/api/services").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.owners.length()").value(2))
            .andExpect(jsonPath("$.teams[0].name").value("Checkout"))
            .andExpect(jsonPath("$.repositoryUrl").value("https://github.com/codelitz/checkout"))
            .andExpect(jsonPath("$.destinations[0].links.length()").value(2))
            .andExpect(jsonPath("$.destinations[0].links[0].environment.name").value("Production"))
            .andExpect(jsonPath("$.destinations[0].links[0].authenticationMethod").value("SSO"))
            .andExpect(jsonPath("$.destinations[0].links[0].accounts.length()").value(2))
            .andExpect(jsonPath("$.destinations[0].links[0].accounts[0].label").value("Administrator"))
            .andExpect(jsonPath("$.destinations[0].links[0].accounts[0].identifier").value("admin@example.com"))
            .andExpect(jsonPath("$.destinations[0].links[0].accounts[0].authenticationMethod").value("SSO"))
            .andExpect(jsonPath("$.destinations[0].links[0].accounts[1].label").value("Marketing"))
            .andExpect(jsonPath("$.destinations[0].links[0].accounts[1].authenticationMethod").value("Sign in"))
            .andExpect(jsonPath("$.destinations[0].links[0].accessNotes").value("VPN required outside the office"))
            .andExpect(jsonPath("$.destinations[0].links[0].accessUrl").value("https://access.example.com/metrics"))
            .andExpect(jsonPath("$.destinations[0].links[1].environment.name").value("Development"));

        mvc.perform(get("/api/services").param("query", "Product engineering"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/services/environments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'Production')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'Development')]").exists());
        mvc.perform(get("/api/teams"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.name == 'Checkout')]").exists());
    }

    @Test
    void rejectsAServiceWithoutARepository() throws Exception {
        var body = """
            {"name":"worker","description":"Runs jobs","owners":["Platform"],"teams":["Checkout"],"lifecycle":"production","tags":[],
             "destinations":[{"name":"Logs","label":"logs","links":[{"url":"https://logs.example.com"}]}]}
            """;
        mvc.perform(post("/api/services").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsPlatformDestinationLabels() throws Exception {
        mvc.perform(post("/api/teams").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"Platform","description":"Owns internal platforms","owners":["Platform on-call"]}
            """)).andExpect(status().isCreated());
        var body = """
            {
              "name": "infra-portal-api",
              "description": "Backend API for the infrastructure portal",
              "owners": ["Platform on-call", "Developer experience"],
              "teams": ["Platform"],
              "lifecycle": "production",
              "repositoryUrl": "https://github.com/codelitz/infra-portal",
              "tags": ["java", "spring-boot", "internal-platform"],
              "destinations": [
                {"name":"Grafana dashboard", "label":"grafana", "links":[{"url":"https://grafana.example.com/d/infra-portal", "environment":"Production"}]},
                {"name":"Argo CD application", "label":"argocd", "links":[{"url":"https://argocd.example.com/applications/infra-portal-api", "environment":"Production"}]},
                {"name":"Confluence runbook", "label":"confluence", "links":[{"url":"https://confluence.example.com/display/PLATFORM/Infra+Portal+API", "environment":"Production"}]},
                {"name":"Sentry project", "label":"sentry", "links":[{"url":"https://sentry.example.com/organizations/codelitz/projects/infra-portal-api", "environment":"Production"}]},
                {"name":"Swagger UI", "label":"swagger", "links":[{"url":"https://infra-portal.example.com/api/docs", "environment":"Production"}]},
                {"name":"Feature flags", "label":"feature-flags", "links":[{"url":"https://flags.example.com/infra-portal", "environment":"Production"}]}
              ]
            }
            """;

        mvc.perform(post("/api/services").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.destinations.length()").value(6))
            .andExpect(jsonPath("$.destinations[0].label").value("grafana"))
            .andExpect(jsonPath("$.destinations[1].label").value("argocd"))
            .andExpect(jsonPath("$.destinations[2].label").value("confluence"))
            .andExpect(jsonPath("$.destinations[3].label").value("sentry"))
            .andExpect(jsonPath("$.destinations[4].label").value("swagger"))
            .andExpect(jsonPath("$.destinations[5].label").value("feature-flags"));
    }

    @Test
    void addsDestinationWhenEditingAService() throws Exception {
        var body = """
            {
              "name": "orders-api",
              "description": "Accepts and manages customer orders.",
              "owners": ["Orders on-call", "orders@example.com"],
              "teams": ["Platform Team"],
              "lifecycle": "production",
              "repositoryUrl": "https://github.com/example/orders-api",
              "tags": ["java", "spring-boot", "orders"],
              "destinations": [
                {"name":"Grafana", "label":"grafana", "links":[
                  {"url":"https://grafana.example.com/d/orders-api", "environment":"Production"},
                  {"url":"https://grafana.example.com/d/orders-api-integration", "environment":"Integration"}
                ]},
                {"name":"Argo CD", "label":"argocd", "links":[{"url":"https://argocd.example.com/applications/orders-api", "environment":"Production"}]},
                {"name":"Runbook", "label":"confluence", "links":[{"url":"https://confluence.example.com/display/COMMERCE/Orders+API"}]},
                {"name":"Sentry", "label":"sentry", "links":[{"url":"https://sentry.example.com/projects/orders-api", "environment":"Production"}]},
                {"name":"Swagger UI", "label":"swagger", "links":[{"url":"https://orders.example.com/swagger-ui.html"}]},
                {"name":"Logs", "label":"logs", "links":[{"url":"https://logs.example.com/orders-staging", "environment":"Staging"}]}
              ]
            }
            """;

        mvc.perform(put("/api/services/30000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.destinations.length()").value(6))
            .andExpect(jsonPath("$.destinations[0].links.length()").value(2))
            .andExpect(jsonPath("$.destinations[0].links[1].environment.name").value("Integration"))
            .andExpect(jsonPath("$.destinations[5].links[0].environment.name").value("Staging"));
    }

    @Test
    void exposesConfiguredCompanyContext() throws Exception {
        mvc.perform(get("/api/context"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companyName").value("Codelitz Labs"));
    }

    @Test
    void deletesOnlyTeamsWithoutServices() throws Exception {
        var response = mvc.perform(post("/api/teams").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"Temporary team","description":"Safe to remove","owners":["Temporary owner"]}
            """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        String teamId = JsonPath.read(response, "$.id");

        mvc.perform(delete("/api/teams/{id}", teamId))
            .andExpect(status().isNoContent());
        mvc.perform(delete("/api/teams/10000000-0000-0000-0000-000000000001"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Team 'Platform Team' cannot be deleted while it has services"));
    }

    @Test
    void providesAndImportsCatalogTemplate() throws Exception {
        mvc.perform(get("/api/import/template"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams[0].name").value("Platform"))
            .andExpect(jsonPath("$.services[0].destinations.length()").value(3));

        var file = new MockMultipartFile("file", "catalog-template.json", MediaType.APPLICATION_JSON_VALUE, """
            {
              "teams": [
                {"name":"Fulfillment","description":"Owns fulfillment flows","owners":["Grace Hopper"]}
              ],
              "services": [
                {
                  "name": "fulfillment-api",
                  "description": "Coordinates fulfillment work",
                  "owners": ["Fulfillment on-call"],
                  "teams": ["Fulfillment"],
                  "lifecycle": "production",
                  "repositoryUrl": "https://github.com/example/fulfillment-api",
                  "tags": ["java"],
                  "destinations": [
                    {"name":"Grafana", "label":"grafana", "links":[{"url":"https://grafana.example.com/d/fulfillment", "environment":"Production"}]},
                    {"name":"Argo CD", "label":"argocd", "links":[{"url":"https://argocd.example.com/applications/fulfillment-staging", "environment":"Staging"}]},
                    {"name":"Runbook", "label":"confluence", "links":[{"url":"https://confluence.example.com/display/FUL/Runbook"}]}
                  ]
                }
              ]
            }
            """.getBytes());

        mvc.perform(multipart("/api/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams").value(1))
            .andExpect(jsonPath("$.services").value(1));
        mvc.perform(get("/api/services").param("query", "fulfillment-api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].lifecycle").value("production"))
            .andExpect(jsonPath("$.content[0].destinations[0].links[0].environment.name").value("Production"))
            .andExpect(jsonPath("$.content[0].destinations[1].links[0].environment.name").value("Staging"));
    }

    @Test
    void exportsCatalogInTheReusableImportFormat() throws Exception {
        mvc.perform(get("/api/import/export"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.matchesPattern("attachment; filename=\"catalog-export-\\d{4}-\\d{2}-\\d{2}\\.json\"")))
            .andExpect(jsonPath("$.teams[0].id").doesNotExist())
            .andExpect(jsonPath("$.teams[0].name").isNotEmpty())
            .andExpect(jsonPath("$.services[0].id").doesNotExist())
            .andExpect(jsonPath("$.services[0].teams[0]").isString())
            .andExpect(jsonPath("$.services[0].destinations[0].links[0].environment").isString());
    }

    @Test
    void rejectsAnInvalidUploadedCatalog() throws Exception {
        var file = new MockMultipartFile("file", "invalid.json", MediaType.APPLICATION_JSON_VALUE, """
            {"teams":[],"services":[{"name":"","description":"","owners":[],"teams":[],"lifecycle":"unknown"}]}
            """.getBytes());

        mvc.perform(multipart("/api/import").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("The uploaded catalog contains invalid fields"));
    }
}
