package com.yunyou.shanhe.user.job;

import com.yunyou.shanhe.user.service.RealNameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 实名档案归档定时任务。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RealNameArchiveJob {

    private final RealNameService realNameService;

    @Value("${realname.archive-days:90}")
    private int archiveDays;

    @Scheduled(cron = "0 0 3 * * ?")
    public void archive() {
        int count = realNameService.archiveExpired(archiveDays);
        log.info("实名档案归档完成，归档 {} 条", count);
    }
}
