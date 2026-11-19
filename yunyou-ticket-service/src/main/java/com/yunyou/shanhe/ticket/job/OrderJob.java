package com.yunyou.shanhe.ticket.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yunyou.shanhe.ticket.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单相关定时任务。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderJob {

    private final BookingService bookingService;

    /**
     * 关闭支付超时的现货订单，并释放景区锁票。
     */
    @XxlJob("orderCloseJob")
    public void orderClose() {
        int count = bookingService.closeExpiredOrders();
        String msg = "超时订单关闭完成，关闭 " + count + " 单";
        log.info(msg);
        XxlJobHelper.handleSuccess(msg);
    }
}
