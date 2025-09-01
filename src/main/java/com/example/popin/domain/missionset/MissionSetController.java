package com.example.popin.domain.missionset;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mission-sets")
@RequiredArgsConstructor
public class MissionSetController {
    private final MissionSetService missionSetService;

    @PostMapping
    public ResponseEntity<MissionSet> create(@RequestBody MissionSet missionSet) {
        return ResponseEntity.ok(missionSetService.create(missionSet));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionSet> get(@PathVariable Long id) {
        return missionSetService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
