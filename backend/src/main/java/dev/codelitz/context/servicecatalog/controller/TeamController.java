package dev.codelitz.context.servicecatalog.controller;

import dev.codelitz.context.servicecatalog.dto.TeamRequest;
import dev.codelitz.context.servicecatalog.dto.TeamResponse;
import dev.codelitz.context.servicecatalog.service.TeamService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
class TeamController {
    private final TeamService service;
    TeamController(TeamService service) { this.service = service; }
    @GetMapping List<TeamResponse> findAll() { return service.findAll(); }
    @PostMapping ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/teams/" + created.id())).body(created);
    }
    @DeleteMapping("/{id}") ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
