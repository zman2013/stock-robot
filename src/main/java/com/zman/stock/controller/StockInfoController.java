package com.zman.stock.controller;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zman.stock.data.domain.CashFlowData;
import com.zman.stock.data.domain.StockFinanceBO;
import com.zman.stock.downloader.StockDetailedFinanceDownloader;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.StockDataTools;

@Controller
@RequestMapping("/stock")
public class StockInfoController {

    @Autowired
    private StockDetailedFinanceDownloader detailedFinanceDownloader;
    @Autowired
    private StockDataService stockDataService;

    @RequestMapping("cash-flow")
    public String cashFlow(String code, Model model) throws Exception {
        List<StockFinanceBO> financeList = detailedFinanceDownloader
                .findByStockList(Arrays.asList(code), "CashFlow");
        StockFinanceBO finance = financeList.get(0);
        Map<String, Map<String, String>> basicFinance = stockDataService
                .getBasicFinanceData(code);

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

        List<CashFlowData> cashFlowDataList = new LinkedList<>();
        // 提取信息
        // 营收和净利润
        for (String item : financeItemArray) { // 财务指标
            CashFlowData cashFlowData = new CashFlowData();
            cashFlowData.item = item;
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

                    cashFlowData.value.add(value);
                } else {
                    cashFlowData.value.add("");
                }
            }
            cashFlowDataList.add(cashFlowData);
        }
        // 现金流
        for (String item : cashItemArray) { // 财务指标
            CashFlowData cashFlowData = new CashFlowData();
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
            cashFlowDataList.add(cashFlowData);
        }

        model.addAttribute("reportDateList", cashDateArray);
        model.addAttribute("dataList", cashFlowDataList);
        model.addAttribute("code", code);
        model.addAttribute("name", finance.getName());

        return "stock/cash-flow";
    }
}
