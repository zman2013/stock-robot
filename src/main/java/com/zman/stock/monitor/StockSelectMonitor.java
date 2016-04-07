package com.zman.stock.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zman.stock.selector.SelectStockData;
import com.zman.stock.service.EmailService;
import com.zman.stock.service.LoadSelectStockService;

@Service
public class StockSelectMonitor {
    private static final Logger logger = LoggerFactory
            .getLogger(StockSelectMonitor.class);

    @Autowired
    private LoadSelectStockService loadSelectStockService;
    @Autowired
    private EmailService emailService;

    public void monitor() {
        // 检测按季度财务数据选股的变动
        StringBuilder result = new StringBuilder();
        try {
            String tmp = compareChanges(loadSelectStockService.loadQuarter(),
                    loadSelectStockService.loadQuarterBackup());
            result.append("[[[季度]]]").append(tmp);
        } catch (Exception e) {
            logger.error("对比季度选股变动时出错", e);
        }
        // 检测按年度财务数据选股的变动
        try {
            String tmp = compareChanges(loadSelectStockService.loadAnnual(),
                    loadSelectStockService.loadAnnualBackup());
            result.append("[[[年度]]]").append(tmp);
        } catch (Exception e) {
            logger.error("对比年度选股变动时出错", e);
        }
        // 检测按交集（季度、年度）财务数据选股的变动
        try {
            String tmp = compareChanges(loadSelectStockService.loadBoth(),
                    loadSelectStockService.loadBothBackup());
            result.append("[[[交集]]]").append(tmp);
        } catch (Exception e) {
            logger.error("对比交集选股变动时出错", e);
        }

        if (result.toString().length() > 24) {
            try {
                logger.info("筛选的股票有变动，请及时关注:\r\n{}", result.toString());
                emailService.send("筛选的股票有变动，请及时关注", result.toString());
            } catch (MessagingException e) {
                logger.error("筛选的股票有变动，发送邮件失败", e);
            }
        } else {
            logger.info("筛选的股票没有变动");
        }
    }

    /**
     * 
     * @param loadQuarter
     * @param loadBothBackup
     * @return
     */
    private String compareChanges(List<SelectStockData> newData,
            List<SelectStockData> oldData) {
        Map<String, SelectStockData> newDataMap = new HashMap<>();
        for (SelectStockData stock : newData) {
            newDataMap.put(stock.code, stock);
        }
        Map<String, SelectStockData> oldDataMap = new HashMap<>();
        for (SelectStockData stock : oldData) {
            oldDataMap.put(stock.code, stock);
        }
        // 获得新增的股票
        List<SelectStockData> newStockList = new LinkedList<>();
        for (SelectStockData stock : newData) {
            if (!oldDataMap.containsKey(stock.code)) {
                newStockList.add(stock);
            }
        }
        // 获得消失的股票
        List<SelectStockData> removedStockList = new LinkedList<>();
        for (SelectStockData stock : oldData) {
            if (!newDataMap.containsKey(stock.code)) {
                removedStockList.add(stock);
            }
        }
        // 生成数据
        return generateContent(newStockList, removedStockList);
    }

    private String generateContent(List<SelectStockData> newStockList,
            List<SelectStockData> removedStockList) {

        StringBuilder sb = new StringBuilder();
        sb.append("新增的股票\r\n");
        for (SelectStockData stock : newStockList) {
            sb.append(stock.code).append("\t");
            sb.append(stock.name).append("\t");
            sb.append(stock.mainBusiness).append("\r\n");
        }
        sb.append("移除的股票\r\n");
        for (SelectStockData stock : removedStockList) {
            sb.append(stock.code).append("\t");
            sb.append(stock.name).append("\t");
            sb.append(stock.mainBusiness).append("\r\n");
        }

        if (newStockList.isEmpty() && removedStockList.isEmpty()) {
            return "";
        } else {
            return sb.toString();
        }
    }
}
