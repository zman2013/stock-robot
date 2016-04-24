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

import com.zman.stock.data.domain.FinanceForecast;
import com.zman.stock.data.domain.SelectedStockChangeInfo;
import com.zman.stock.selector.GenerateHoldStockFinanceInfo;
import com.zman.stock.selector.SelectStockData;
import com.zman.stock.service.EmailService;
import com.zman.stock.service.LoadSelectStockService;
import com.zman.stock.service.StockDataService;
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
    private StockDataService stockDataService;
    @Autowired
    private GenerateHoldStockFinanceInfo holdStockFinanceGenerator;

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
        // 检测持股的财务数据变动
     // 检测按交集（季度、年度）财务数据选股的变动
        SelectedStockChangeInfo holdChangeInfo = null;
        try {
            holdChangeInfo = compareChanges(
                    loadSelectStockService.loadHoldStockData(),
                    loadSelectStockService.loadHoldBackup());
        } catch (Exception e) {
            logger.error("对比交集选股变动时出错", e);
        }
        // 检查持有的股票是否有新的业绩预告
        Map<String, FinanceForecast> financeForecastMap = checkFinanceForecast();

        // 生成邮件，并发送
        if (quarterChangeInfo != null || annualChangeInfo != null
                || bothChangeInfo != null || holdChangeInfo != null) {
            try {
                ThymeleafTemplateContext ctx = new ThymeleafTemplateContext();
                ctx.setVariable("quarterChangeInfo", quarterChangeInfo);
                ctx.setVariable("annualChangeInfo", annualChangeInfo);
                ctx.setVariable("bothChangeInfo", bothChangeInfo);
                ctx.setVariable("holdChangeInfo", holdChangeInfo);
                ctx.setVariable("holdStockFinanceForecast", financeForecastMap);

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

        // 生成持股财务信息
        try {
            holdStockFinanceGenerator.generateFinanceInfo();
        } catch (Exception e) {
            logger.error("生成持股财务信息时出错", e);
        }
    }

    /**
     * 检查持有的股票是否有财务预告
     * 
     * @return
     */
    private Map<String,FinanceForecast> checkFinanceForecast(){
        Map<String, FinanceForecast> result = new HashMap<>();

        try {
            Map<String, FinanceForecast> map = stockDataService
                    .loadFinanceForecast();
            loadSelectStockService.loadHoldStockData().forEach(stock -> {
                if (map.containsKey(stock.code)) {
                    result.put(stock.code, map.get(stock.code));
                }
            });
        } catch (Exception e) {
            logger.error("检查财务预告出错", e);
        }
        return result;
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
