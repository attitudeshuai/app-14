package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.dto.ReputationDTO;
import com.petfoster.service.ReputationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reputation")
@RequiredArgsConstructor
public class ReputationController {

    private final ReputationService reputationService;

    @GetMapping("/{userId}")
    public ApiResponse<ReputationDTO> getReputation(@PathVariable Long userId) {
        log.info("查询用户信誉分: userId={}", userId);
        ReputationDTO reputation = reputationService.calculateReputation(userId);
        return ApiResponse.success(reputation);
    }

    @GetMapping("/mine")
    public ApiResponse<ReputationDTO> getMyReputation(
            @RequestAttribute("userId") Long currentUserId) {
        log.info("查询当前用户信誉分: userId={}", currentUserId);
        ReputationDTO reputation = reputationService.calculateReputation(currentUserId);
        return ApiResponse.success(reputation);
    }
}
