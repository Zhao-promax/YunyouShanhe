package com.yunyou.shanhe.ticket.controller;

import com.yunyou.shanhe.common.core.Result;
import com.yunyou.shanhe.ticket.entity.TicketType;
import com.yunyou.shanhe.ticket.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 票种价格维护（运营后台使用）。
 */
@RestController
@RequestMapping("/api/ticket/type")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping("/list")
    public Result<List<TicketType>> list(@RequestParam(required = false) Long scenicId) {
        return Result.ok(ticketTypeService.listByScenic(scenicId));
    }

    @PostMapping
    public Result<TicketType> save(@RequestBody TicketType type) {
        return Result.ok(ticketTypeService.save(type));
    }
}
