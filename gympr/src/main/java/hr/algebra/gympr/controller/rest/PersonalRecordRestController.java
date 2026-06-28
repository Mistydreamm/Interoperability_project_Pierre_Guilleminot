package hr.algebra.gympr.controller.rest;

import hr.algebra.gympr.dto.PersonalRecordDto;
import hr.algebra.gympr.entity.User;
import hr.algebra.gympr.enums.LiftType;
import hr.algebra.gympr.enums.MuscleGroup;
import hr.algebra.gympr.service.PersonalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lifts")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Personal Records", description = "Personal record CRUD and search")
public class PersonalRecordRestController {

    private final PersonalRecordService recordService;

    public PersonalRecordRestController(PersonalRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    @Operation(summary = "Get all personal records")
    public ResponseEntity<List<PersonalRecordDto>> getAll() {
        return ResponseEntity.ok(recordService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single record by ID")
    public ResponseEntity<PersonalRecordDto> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recordService.findById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search records by lift, muscle group, milestone or text")
    public ResponseEntity<List<PersonalRecordDto>> search(
        @RequestParam(required = false) LiftType lift,
        @RequestParam(required = false) MuscleGroup group,
        @RequestParam(required = false) Boolean milestone,
        @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(recordService.search(lift, group, milestone, query));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Log a new PR (admin only)")
    public ResponseEntity<PersonalRecordDto> create(
        @Valid @RequestBody PersonalRecordDto dto,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(dto, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a PR (admin only)")
    public ResponseEntity<PersonalRecordDto> update(
        @PathVariable Long id,
        @Valid @RequestBody PersonalRecordDto dto
    ) {
        try {
            return ResponseEntity.ok(recordService.update(id, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a PR (admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            recordService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
