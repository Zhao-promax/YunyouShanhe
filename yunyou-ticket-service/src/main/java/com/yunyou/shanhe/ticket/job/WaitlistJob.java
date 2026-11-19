package com.yunyou.shanhe.ticket.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
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

    /**
     * 候补队列对账补偿：Redis 排序快照以数据库为准重建。
     * 在 XXL-Job 后台新建任务，JobHandler 填 waitlistReconcileJob，cron 建议每 10 分钟执行一次。
     */
    @XxlJob("waitlistReconcileJob")
    public void waitlistReconcile() {
        int groups = waitlistService.reconcile();
        String msg = "候补队列对账完成，重建 " + groups + " 组";
        log.info(msg);
        XxlJobHelper.handleSuccess(msg);
    }
}
