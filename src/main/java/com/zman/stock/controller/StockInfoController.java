package com.zman.stock.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.zman.stock.data.domain.MainFinanceData;
import com.zman.stock.data.domain.PEHistory;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.data.domain.StockFinanceBO;
import com.zman.stock.data.domain.StockPrice;
import com.zman.stock.downloader.StockDetailedFinanceDownloader;
import com.zman.stock.downloader.StockFuQuanQianMonthPriceHistoryDownloader;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.StockDataTools;

@Controller
@RequestMapping("/stock")
public class StockInfoController {

    private final Logger logger = LoggerFactory.getLogger(StockInfoController.class);

    @Autowired
    private StockDetailedFinanceDownloader detailedFinanceDownloader;
    @Autowired
    private StockDataService stockDataService;
    @Autowired
    private StockFuQuanQianMonthPriceHistoryDownloader stockPriceHistoryDownloader;

    @RequestMapping("main-finance")
    public String mainFinance(
            String code,
            @RequestParam(name = "pe-start-date", defaultValue = "20130101") int peStartDate,
            Model model) throws Exception {
        logger.info("start...");
        List<StockFinanceBO> financeList = detailedFinanceDownloader
                .findByStockList(Arrays.asList(code), "CashFlow");
        logger.info("downloaded CashFlow");
        List<StockFinanceBO> balanceFinanceList = detailedFinanceDownloader.findByStockList(Arrays.asList(code),"BalanceSheet");
        logger.info("downloaded balanceFinance");
        List<StockFinanceBO> profitFinanceList = detailedFinanceDownloader.findByStockList(Arrays.asList(code),"ProfitStatement");
        logger.info("downloaded profit");
        StockFinanceBO finance = financeList.get(0);
        Map<String, Map<String, String>> basicFinance = stockDataService
                .getBasicFinanceData(code);
        StockBasicInfo stockBasicInfo = stockDataService.getAllStockBasicInfo()
                .get(code);

        // 财报日期
        List<String> cashDateArray = StockDataTools
                .computeDetailFinanceReportDate();
        List<String> financeDateArray = StockDataTools
                .computeBasicFinanceReportDate();
        // 财务指标
        String[] cashItemArray = new String[] { "经营活动产生的现金流量净额",
                "投资活动产生的现金流量净额", "筹资活动产生的现金流量净额" };
        String[] financeItemArray = new String[] { "营业收入", "收入同比增长率", "净利润",
                "净利润同比增长率" };

        List<MainFinanceData> mainFinanceDataList = new LinkedList<>();
        // 提取信息
        // 营收和净利润
        mainFinanceDataList.addAll(findProfitData(financeItemArray,
                financeDateArray, basicFinance));
        // 现金流
        mainFinanceDataList.addAll(findCashFlowData(cashItemArray,
                cashDateArray, finance));
        // 净资产收益率
        mainFinanceDataList.add(findJingzichanShouyilv(cashDateArray,balanceFinanceList.get(0),profitFinanceList.get(0)));
        //

        // 获得股票的前复权月级价格历史数据
        logger.info("start downloading price history");
        List<StockPrice> stockPriceList = stockPriceHistoryDownloader
                .download(code,Float.parseFloat(stockBasicInfo.price));
        logger.info("downloaded price history");

        PEHistory peHistory = computePEHistory(stockPriceList,
                stockBasicInfo.code, stockBasicInfo.count, basicFinance,
                peStartDate);

        PEHistory pbHistory = computePBHistory(stockPriceList,
                stockBasicInfo.count, balanceFinanceList.get(0), //只有一个股票
                peStartDate);

        model.addAttribute("reportDateList", cashDateArray);
        model.addAttribute("dataList", mainFinanceDataList);
        model.addAttribute("code", code);
        model.addAttribute("name", finance.getName());
        model.addAttribute("peHistory", peHistory);
        model.addAttribute("pbHistory", pbHistory);

        return "stock/main-finance";
    }

    /**
     * 净资产收益率
     * @param cashDateArray
     * @return
     */
    private MainFinanceData findJingzichanShouyilv(List<String> cashDateArray, StockFinanceBO balanceFinance,
                                                                         StockFinanceBO profitFinance) {
        MainFinanceData financeData = new MainFinanceData();
        financeData.item = "净资产收益率";
        for (String date : cashDateArray) { // 报告期
            if (balanceFinance.getData().get("归属于母公司股东权益合计").containsKey(date)) {
                float quanyi = balanceFinance.getData().get("归属于母公司股东权益合计").get(date);
                float profit = profitFinance.getData().get("归属于母公司所有者的净利润").get(date);
                financeData.value.add(String.format("%.2f\t",profit/quanyi*100));
            } else {
                financeData.value.add("");
            }
        }
        return financeData;
    }

    private PEHistory computePBHistory(List<StockPrice> stockPriceList, long count, StockFinanceBO balanceFinance, int peStartDate) {
        PEHistory pbHistory = new PEHistory();

        int i = 0;
        for (StockPrice stockPrice : stockPriceList) {

            if (Integer.parseInt(stockPrice.date) < peStartDate) {
                continue;
            }

            // 获取当前日期的净资产

            double jingzichan = 0;
            try {
                //日期转换，转为上一季度末yyyy-03-31，yyyy-60-30、、、
                jingzichan = StockDataTools.findJingzichan(stockPrice.date, balanceFinance);
            } catch (Exception e) {
                continue;
            }

            if (i++ % 3 == 0) {
                pbHistory.dateList.add(stockPrice.date);
            } else {
                pbHistory.dateList.add("");
            }
            // 根据最大价格计算pe
            double pb = stockPrice.maxPrice * count / jingzichan;
            pb = Double.parseDouble(String.format("%.2f", pb));
            pbHistory.maxPeList.add((float) pb);
            // 根据最小价格计算pe
            pb = stockPrice.minPrice * count / jingzichan;
            pb = Double.parseDouble(String.format("%.2f", pb));
            pbHistory.minPeList.add((float) pb);
        }

        return pbHistory;
    }

    /**
     * 提取信息：现金流
     * 
     * @param cashItemArray
     * @param cashDateArray
     * @param finance
     * @return
     */
    private Collection<MainFinanceData> findCashFlowData(
            String[] cashItemArray, List<String> cashDateArray,
            StockFinanceBO finance) {
        List<MainFinanceData> result = new LinkedList<>();
        for (String item : cashItemArray) { // 财务指标
            MainFinanceData cashFlowData = new MainFinanceData();
            cashFlowData.item = item;
            for (String date : cashDateArray) { // 报告期
                if (finance.getData().get(item).containsKey(date)) {
                    String value = String.format("%.2f\t", finance.getData()
                            .get(item).get(date) / 100000000);
                    cashFlowData.value.add(value);
                } else {
                    cashFlowData.value.add("");
                }
            }
            result.add(cashFlowData);
        }
        return result;
    }

    /**
     * 提取信息：营收和净利润
     * 
     * @param financeItemArray
     * @param financeDateArray
     * @param basicFinance
     * @return
     */
    private Collection<MainFinanceData> findProfitData(
            String[] financeItemArray, List<String> financeDateArray,
            Map<String, Map<String, String>> basicFinance) {
        List<MainFinanceData> result = new LinkedList<>();

        for (String item : financeItemArray) { // 财务指标
            MainFinanceData mainFinanceData = new MainFinanceData();
            mainFinanceData.item = item;
            for (String date : financeDateArray) { // 报告期
                if (basicFinance.containsKey(date)) {
                    double tmp = Double.parseDouble(basicFinance.get(date).get(
                            item));
                    String value = "";
                    if (!item.endsWith("率")) {
                        tmp = tmp / 100000000;// 除以一亿
                        value = String.format("%.2f", tmp);
                    } else {
                        value = String.format("%.2f%%", tmp);
                    }

                    mainFinanceData.value.add(value);
                } else {
                    mainFinanceData.value.add("");
                }
            }
            result.add(mainFinanceData);
        }
        return result;
    }

    private PEHistory computePEHistory(List<StockPrice> stockPriceList,
            String code, long count,
            Map<String, Map<String, String>> basicFinance, int peStartDate) {

        PEHistory peHistory = new PEHistory();

        int i = 0;
        for (StockPrice stockPrice : stockPriceList) {

            if (Integer.parseInt(stockPrice.date) < peStartDate) {
                continue;
            }

            // 计算此日期之前一年的净利润

            double profit = 0;
            try {
                profit = StockDataTools.findProfit(stockPrice.date,
                        basicFinance);
            } catch (Exception e) {
                continue;
            }

            if (i++ % 3 == 0) {
                peHistory.dateList.add(stockPrice.date);
            } else {
                peHistory.dateList.add("");
            }
            // 根据最大价格计算pe
            double pe = stockPrice.maxPrice * count / profit;
            pe = Double.parseDouble(String.format("%.2f", pe));
            peHistory.maxPeList.add((float) pe);
            // 根据最小价格计算pe
            pe = stockPrice.minPrice * count / profit;
            pe = Double.parseDouble(String.format("%.2f", pe));
            peHistory.minPeList.add((float) pe);
        }

        return peHistory;
    }

}
