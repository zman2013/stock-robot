package com.zman.stock.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        List<StockFinanceBO> financeList = detailedFinanceDownloader
                .findByStockList(Arrays.asList(code), "CashFlow");
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

        // 获得股票的前复权月级价格历史数据
        List<StockPrice> stockPriceList = stockPriceHistoryDownloader
                .download(code);

        PEHistory peHistory = computePEHistory(stockPriceList,
                stockBasicInfo.code, stockBasicInfo.count, basicFinance,
                peStartDate);

        model.addAttribute("reportDateList", cashDateArray);
        model.addAttribute("dataList", mainFinanceDataList);
        model.addAttribute("code", code);
        model.addAttribute("name", finance.getName());
        model.addAttribute("peHistory", peHistory);

        return "stock/main-finance";
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
                System.out.println(stockPrice.date);
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
