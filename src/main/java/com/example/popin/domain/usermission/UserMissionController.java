package com.example.popin.domain.usermission;

import com.example.popin.domain.usermission.dto.SubmitAnswerRequestDto;
import com.example.popin.domain.usermission.dto.SubmitAnswerResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user-missions")
@Validated
public class UserMissionController {

    private final UserMissionService userMissionService;

    public UserMissionController(UserMissionService userMissionService) {
        this.userMissionService = userMissionService;
    }

    @PostMapping
    public ResponseEntity<UserMission> create(@RequestBody UserMission userMission) {
        return ResponseEntity.ok(userMissionService.create(userMission));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserMission> get(@PathVariable Long id) {
        Optional<UserMission> found = userMissionService.findById(id);
        if (found.isPresent()) return ResponseEntity.ok(found.get());
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{missionId}/submit-answer")
    public ResponseEntity<SubmitAnswerResponseDto> submitAnswer(
            @PathVariable Long missionId,
            @RequestBody @Valid SubmitAnswerRequestDto req
    ) {
        SubmitAnswerResponseDto res =
                userMissionService.submitAnswer(missionId, req.getUserId(), req.getAnswer());


        HttpStatus status = res.isPass() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<SubmitAnswerResponseDto>(res, status);
    }
}
