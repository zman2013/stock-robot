package com.zman.stock;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 检查目录是否存在
 * 
 * @author zman
 *
 */
@Component
public class DirectoryInitializer {

    @Value("${stock.select.dir}")
    private String selectDir;
    @Value("${stock.basic.finance.dir}")
    private String basicFinanceDir;
    @Value("${stock.detailed.finance.dir}")
    private String detailedFinanceDir;

    @PostConstruct
    public void init() {
        File file = new File(selectDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(basicFinanceDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(detailedFinanceDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}
