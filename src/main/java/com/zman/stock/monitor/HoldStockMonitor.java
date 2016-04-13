package com.zman.stock.monitor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.service.EmailService;
import com.zman.stock.service.StockDataService;
import com.zman.stock.template.ThymeleafTemplateContext;

/**
 * 持有的股票状态监控，决定是否卖出
 * 
 * @author zman
 *
 */
@Service
public class HoldStockMonitor {

    private static final Logger logger = LoggerFactory
            .getLogger(HoldStockMonitor.class);

    @Autowired
    private StockDataService stockDataService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private TemplateEngine templateEngine;

    public void monitor() throws Exception {

        Map<String, HoldStockInfo> holdStockMap = stockDataService
                .loadHoldStockInfo();

        boolean isNeedAction = false;
        // 检查是否需要进行股票操作
        for (HoldStockInfo stock : holdStockMap.values()) {
            if (!"持有".equals(stock.action)) {
                isNeedAction = true;
                break;
            }
        }
        //生成内容
        ThymeleafTemplateContext ctx = new ThymeleafTemplateContext();
        ctx.setVariable("stockMap", holdStockMap);
        String content = templateEngine.process("email/hold-stock-alarm",
 ctx);
        
        // 检查是否需要发送邮件
        if (isNeedAction) {
            logger.info("需要进行股票操作:{}", content);
            emailService.send("请进行股票操作", content);
        } else {
            logger.info("不需要进行股票操作，继续持有:{}", content);
        }

    }
}
