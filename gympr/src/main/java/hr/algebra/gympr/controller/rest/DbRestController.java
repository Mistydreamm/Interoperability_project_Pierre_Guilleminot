package hr.algebra.gympr.controller.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/api/database")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Database ", description = "Database's backup and restore management")
public class DbRestController {


    private JdbcTemplate jdbcTemplate;
    public DbRestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @PostMapping("/backup")
    public ResponseEntity<String> backup() {
        try {
            jdbcTemplate.execute("SCRIPT TO 'backup.sql'");
            return ResponseEntity.ok("Backup of the database done with success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Backup failed: " + e.getMessage());
        }
    }
    @PostMapping("/restore")
    public ResponseEntity<String> restore() {
        try {
            File file = new File("backup.sql");
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Restore failed: Backup file 'backup.sql' does not exist.");
            }
            jdbcTemplate.execute("DROP ALL OBJECTS");
            jdbcTemplate.execute("RUNSCRIPT FROM 'backup.sql'");
            return ResponseEntity.ok("Restore ok");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Restore failed: " + e.getMessage());
        }
    }

}
