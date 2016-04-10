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
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;

import com.zman.stock.data.domain.SelectedStockChangeInfo;
import com.zman.stock.selector.SelectStockData;
import com.zman.stock.service.EmailService;
import com.zman.stock.service.LoadSelectStockService;
import com.zman.stock.template.ThymeleafTemplateContext;

@Service
public class StockSelectMonitor {
    private static final Logger logger = LoggerFactory
            .getLogger(StockSelectMonitor.class);

    @Autowired
    private LoadSelectStockService loadSelectStockService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private TemplateEngine templateEngine;

    public void monitor() {
        // 检测按季度财务数据选股的变动
        SelectedStockChangeInfo quarterChangeInfo = null;
        try {
            quarterChangeInfo = compareChanges(
                    loadSelectStockService.loadQuarter(),
                    loadSelectStockService.loadQuarterBackup());
        } catch (Exception e) {
            logger.error("对比季度选股变动时出错", e);
        }
        // 检测按年度财务数据选股的变动
        SelectedStockChangeInfo annualChangeInfo = null;
        try {
            annualChangeInfo = compareChanges(
                    loadSelectStockService.loadAnnual(),
                    loadSelectStockService.loadAnnualBackup());
        } catch (Exception e) {
            logger.error("对比年度选股变动时出错", e);
        }
        // 检测按交集（季度、年度）财务数据选股的变动
        SelectedStockChangeInfo bothChangeInfo = null;
        try {
            bothChangeInfo = compareChanges(loadSelectStockService.loadBoth(),
                    loadSelectStockService.loadBothBackup());
        } catch (Exception e) {
            logger.error("对比交集选股变动时出错", e);
        }

        if (quarterChangeInfo != null || annualChangeInfo != null
                || bothChangeInfo != null) {
            try {
                ThymeleafTemplateContext ctx = new ThymeleafTemplateContext();
                ctx.setVariable("quarterChangeInfo", quarterChangeInfo);
                ctx.setVariable("annualChangeInfo", annualChangeInfo);
                ctx.setVariable("bothChangeInfo", bothChangeInfo);

                String content = templateEngine.process(
                        "email/stock-change-alarm",
                        ctx);

                logger.info("筛选的股票有变动，请及时关注");
                emailService.send("筛选的股票有变动，请及时关注", content);
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
    private SelectedStockChangeInfo compareChanges(
            List<SelectStockData> newData,
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

    private SelectedStockChangeInfo generateContent(
            List<SelectStockData> newStockList,
            List<SelectStockData> removedStockList) {
        SelectedStockChangeInfo changeInfo = new SelectedStockChangeInfo();

        for (SelectStockData stock : newStockList) {
            changeInfo.newStockList.add(stock);
        }
        for (SelectStockData stock : removedStockList) {
            changeInfo.removedStockList.add(stock);
        }

        if (CollectionUtils.isEmpty(changeInfo.newStockList)
                && CollectionUtils.isEmpty(changeInfo.removedStockList)) {
            return null;
        } else {
            return changeInfo;
        }
    }
}
