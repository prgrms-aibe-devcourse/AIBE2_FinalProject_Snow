package com.snow.popin.domain.reward.controller;

import com.snow.popin.domain.reward.dto.request.ClaimRequestDto;
import com.snow.popin.domain.reward.dto.request.RedeemRequestDto;
import com.snow.popin.domain.reward.dto.response.ClaimResponseDto;
import com.snow.popin.domain.reward.dto.response.OptionViewResponseDto;
import com.snow.popin.domain.reward.dto.response.RedeemResponseDto;
import com.snow.popin.domain.reward.dto.response.UserRewardResponseDto;
import com.snow.popin.domain.reward.entity.UserReward;
import com.snow.popin.domain.reward.service.RewardService;
import com.snow.popin.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Validated
public class RewardController {

    private final RewardService rewardService;
    private final UserService userService;

    // 옵션 목록 (잔여량 포함)
    @GetMapping("/options/{missionSetId}")
    public List<OptionViewResponseDto> options(@PathVariable UUID missionSetId) {
        return rewardService.listOptions(missionSetId).stream()
                .map(o -> OptionViewResponseDto.builder()
                        .id(o.getId())
                        .name(o.getName())
                        .total(o.getTotal())
                        .issued(o.getIssued())
                        .remaining(Math.max(0, o.getTotal() - o.getIssued()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    // 발급 (유저당 1회, 재고 차감)
    @PostMapping("/claim")
    public ResponseEntity<ClaimResponseDto> claim(@RequestBody @Valid ClaimRequestDto req,
                                                  Principal principal) {
        Long userId = resolveUserId(principal);
        UserReward rw = rewardService.claim(req.getMissionSetId(), req.getOptionId(), userId);
        return ResponseEntity.ok(
                ClaimResponseDto.builder()
                        .ok(true)
                        .rewardId(rw.getId())
                        .status(rw.getStatus().name())
                        .optionId(rw.getOption().getId())
                        .build()
        );
    }

    // 수령 (PIN 입력)
    @PostMapping("/redeem")
    public ResponseEntity<RedeemResponseDto> redeem(@RequestBody @Valid RedeemRequestDto req,
                                                    Principal principal) {
        Long userId = resolveUserId(principal);
        UserReward rw = rewardService.redeem(req.getMissionSetId(), userId, req.getStaffPin());
        return ResponseEntity.ok(
                RedeemResponseDto.builder()
                        .ok(true)
                        .status(rw.getStatus().name())
                        .redeemedAt(rw.getRedeemedAt())
                        .build()
        );
    }

    // 내 리워드 조회 (이미 발급 받았는지 확인)
    @GetMapping("/my/{missionSetId}")
    public ResponseEntity<UserRewardResponseDto> myReward(@PathVariable UUID missionSetId,
                                                          Principal principal) {
        Long userId = resolveUserId(principal);
        return rewardService.findUserReward(userId, missionSetId)
                .map(rw -> ResponseEntity.ok(
                        UserRewardResponseDto.builder()
                                .ok(true)
                                .status(rw.getStatus().name())
                                .optionId(rw.getOption().getId())
                                .optionName(rw.getOption().getName())
                                .build()
                ))
                .orElse(ResponseEntity.ok(UserRewardResponseDto.builder().ok(false).build()));
    }

    private Long resolveUserId(Principal principal) {
        //TODO: 사용자 받아오기 config로 대체 예정
        if (principal == null) {
            throw new IllegalStateException("로그인 정보가 없습니다.");
        }

        Long id = userService.getUserIdByUsername(principal.getName());
        if (id == null) {
            throw new IllegalStateException("사용자 ID를 찾을 수 없습니다: " + principal.getName());
        }

        return id;
    }

}
