package com.zman.stock.controller;

import com.zman.stock.Scheduler;
import com.zman.stock.downloader.StockBasicFinanceFromSinaDownloader;
import com.zman.stock.downloader.StockHoufuquanDailyPriceHistoryDownloader;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private StockHoufuquanDailyPriceHistoryDownloader priceHistoryDownloader;
    @Autowired
    private StockBasicFinanceFromSinaDownloader stockBasicFinanceFromSinaDownloader;

    /**
     * 刷新股票基本财务信息，并对所有股票进行分析，监控逻辑
     * 
     * @return
     */
    @RequestMapping("stock-analysis")
    @ResponseBody
    public String downloadAndAnalysisStockFinance() {
        scheduler.downloadAndAnalysisStockFinance();
        return "success";
    }

    @RequestMapping("basic-info")
    @ResponseBody
    public String downloadBasicInfo() {
        scheduler.downloadStockBasicInfo();
        return "success";
    }

    @RequestMapping("refresh-basic-finance")
    @ResponseBody
    public String downloadBasicInfo(String code) {
        if(Strings.isEmpty(code)){
            return "success";
        }
        stockBasicFinanceFromSinaDownloader.download(code);
        return "success";
    }

    @RequestMapping("hold-stock")
    @ResponseBody
    public String refreshHoldStockInfo() throws Exception {
        priceHistoryDownloader.download();
        return "success";
    }

}
