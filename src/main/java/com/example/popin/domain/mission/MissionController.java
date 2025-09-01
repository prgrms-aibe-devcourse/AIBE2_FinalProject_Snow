package com.example.popin.domain.mission;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {
    private final MissionService missionService;

    @PostMapping
    public ResponseEntity<Mission> create(@RequestBody Mission mission) {
        return ResponseEntity.ok(missionService.create(mission));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> get(@PathVariable Long id) {
        return missionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
