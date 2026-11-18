package com.yunyou.shanhe.message.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.message.entity.MessageRecord;
import com.yunyou.shanhe.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/inbox")
    public Result<List<MessageRecord>> inbox(@RequestHeader("X-User-Id") Long userId) {
        return Result.ok(messageService.inbox(userId));
    }

    @PostMapping("/{id}/read")
    public Result<Void> read(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        messageService.markRead(userId, id);
        return Result.ok();
    }

    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestHeader("X-User-Id") Long userId, @RequestParam Long scenicId) {
        messageService.subscribe(userId, scenicId);
        return Result.ok();
    }

    @PostMapping("/unsubscribe")
    public Result<Void> unsubscribe(@RequestHeader("X-User-Id") Long userId, @RequestParam Long scenicId) {
        messageService.unsubscribe(userId, scenicId);
        return Result.ok();
    }
}
