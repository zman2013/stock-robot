package com.zman.stock.monitor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.service.EmailService;
import com.zman.stock.service.StockDataService;

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

    private static final ObjectMapper mapper = Jackson2ObjectMapperBuilder
            .json().build();

    @Autowired
    private StockDataService stockDataService;
    @Autowired
    private EmailService emailService;

    public void monitor() throws Exception {
        Map<String, HoldStockInfo> holdStockMap = stockDataService
                .loadHoldStockInfo();

        String content = mapper.writeValueAsString(holdStockMap);
        boolean isNeedAction = false;
        // 检查是否需要进行股票操作
        for (HoldStockInfo stock : holdStockMap.values()) {
            if (!"持有".equals(stock.action)) {
                isNeedAction = true;
                break;
            }
        }
        // 检查是否需要发送邮件
        if (isNeedAction) {
            logger.info("需要进行股票操作:{}", content);
            emailService.send("请进行股票操作", content);
        } else {
            logger.info("不需要进行股票操作，继续持有:{}", content);
        }
    }
}
