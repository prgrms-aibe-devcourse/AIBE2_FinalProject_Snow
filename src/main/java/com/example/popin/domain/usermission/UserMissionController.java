package com.example.popin.domain.usermission;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-missions")
@RequiredArgsConstructor
public class UserMissionController {
    private final UserMissionService userMissionService;

    @PostMapping
    public ResponseEntity<UserMission> create(@RequestBody UserMission userMission) {
        return ResponseEntity.ok(userMissionService.create(userMission));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserMission> get(@PathVariable Long id) {
        return userMissionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
