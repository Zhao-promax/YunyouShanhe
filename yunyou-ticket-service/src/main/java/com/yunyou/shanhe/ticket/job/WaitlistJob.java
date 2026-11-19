package com.yunyou.shanhe.ticket.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yunyou.shanhe.ticket.service.BookingService;
import com.yunyou.shanhe.ticket.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 候补相关定时任务。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WaitlistJob {

    private final WaitlistService waitlistService;
    private final BookingService bookingService;

    /**
     * 候补队列对账补偿：Redis 排序快照以数据库为准重建。
     */
    @XxlJob("waitlistReconcileJob")
    public void waitlistReconcile() {
        int groups = waitlistService.reconcile();
        String msg = "候补队列对账完成，重建 " + groups + " 组";
        log.info(msg);
        XxlJobHelper.handleSuccess(msg);
    }

    /**
     * 候补熔断：终止已到截止时间仍未中签的候补单，并触发退款。
     */
    @XxlJob("waitlistCutoffJob")
    public void waitlistCutoff() {
        int count = bookingService.cutoffExpiredWaitlists();
        String msg = "候补熔断完成，终止 " + count + " 单";
        log.info(msg);
        XxlJobHelper.handleSuccess(msg);
    }
}
