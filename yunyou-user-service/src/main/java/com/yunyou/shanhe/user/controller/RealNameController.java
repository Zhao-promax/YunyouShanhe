package com.yunyou.shanhe.user.controller;

import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.common.core.ErrorCode;
import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.user.dto.RealNameRequest;
import com.yunyou.shanhe.user.entity.RealNameProfile;
import com.yunyou.shanhe.user.service.RealNameService;
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
@RequestMapping("/api/user/realname")
@RequiredArgsConstructor
public class RealNameController {

    private final RealNameService realNameService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Result<RealNameProfile> add(@RequestHeader("Authorization") String authorization,
                                       @Valid @RequestBody RealNameRequest request) {
        Long userId = jwtUtil.getUserId(resolve(authorization));
        return Result.ok(realNameService.add(userId, request));
    }

    @GetMapping("/list")
    public Result<List<RealNameProfile>> list(@RequestHeader("Authorization") String authorization) {
        Long userId = jwtUtil.getUserId(resolve(authorization));
        return Result.ok(realNameService.list(userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String authorization,
                               @PathVariable Long id) {
        Long userId = jwtUtil.getUserId(resolve(authorization));
        realNameService.delete(userId, id);
        return Result.ok();
    }

    private String resolve(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return authorization.substring(7);
    }
}
