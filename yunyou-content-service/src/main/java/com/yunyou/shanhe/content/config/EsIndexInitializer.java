package com.yunyou.shanhe.content.config;

import com.yunyou.shanhe.content.es.GuideDocument;
import com.yunyou.shanhe.content.es.ScenicDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * 启动时按 @Document 映射显式创建 ES 索引（含 IK 分词器）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EsIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations operations;

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing(ScenicDocument.class);
        createIfMissing(GuideDocument.class);
    }

    private void createIfMissing(Class<?> clazz) {
        IndexOperations indexOps = operations.indexOps(clazz);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
            log.info("创建 ES 索引并写入映射: {}", clazz.getSimpleName());
        }
    }
}
