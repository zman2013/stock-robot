package com.zman.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zman.stock.Scheduler;
import com.zman.stock.downloader.StockHoufuquanDailyPriceHistoryDownloader;

@Controller
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private StockHoufuquanDailyPriceHistoryDownloader priceHistoryDownloader;

    /**
     * 激活持有的股票分析，监控逻辑
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

    @RequestMapping("hold-stock")
    @ResponseBody
    public String refreshHoldStockInfo() throws Exception {
        priceHistoryDownloader.download();
        return "success";
    }

}
