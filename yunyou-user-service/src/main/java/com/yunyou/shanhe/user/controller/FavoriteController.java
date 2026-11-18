package com.yunyou.shanhe.user.controller;

import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.common.core.ErrorCode;
import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.user.dto.FavoriteRequest;
import com.yunyou.shanhe.user.entity.UserFavorite;
import com.yunyou.shanhe.user.service.FavoriteService;
import com.yunyou.shanhe.user.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Result<Void> add(@RequestHeader("Authorization") String authorization,
                            @Valid @RequestBody FavoriteRequest request) {
        Long userId = jwtUtil.getUserId(resolve(authorization));
        favoriteService.add(userId, request.targetType(), request.targetId());
        return Result.ok();
    }

    @GetMapping("/list")
    public Result<List<UserFavorite>> list(@RequestHeader("Authorization") String authorization) {
        Long userId = jwtUtil.getUserId(resolve(authorization));
        return Result.ok(favoriteService.list(userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        Long userId = jwtUtil.getUserId(resolve(authorization));
        favoriteService.remove(userId, id);
        return Result.ok();
    }

    private String resolve(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return authorization.substring(7);
    }
}
