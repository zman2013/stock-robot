package com.zman.stock;

import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.downloader.StockBasicFinanceFromSinaDownloader;
import com.zman.stock.downloader.StockBasicInfoSinaDownloader;
import com.zman.stock.downloader.StockCountDownloader;
import com.zman.stock.downloader.StockFinanceForecastDownloader;
import com.zman.stock.downloader.StockHoufuquanDailyPriceHistoryDownloader;
import com.zman.stock.downloader.StockMainBusinessDownloader;
import com.zman.stock.monitor.HoldStockMonitor;
import com.zman.stock.monitor.StockSelectMonitor;
import com.zman.stock.selector.GenerateHoldStockFinanceInfo;
import com.zman.stock.selector.SelectStockByAnnualFinance;
import com.zman.stock.selector.SelectStockByBothInfo;
import com.zman.stock.selector.SelectStockByQuarterFinance;
import com.zman.stock.service.StockDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 一切的开始
 * 
 * 每天： 下载股票基本信息，股票股数，股票主营业务。
 * 
 * 每天： 下载股票基本财务数据->分析季度优秀股票->分析年度优秀股票->分析两者交集->检查晒出选的股票是否有变动。
 * 下载股票后复权股价历史数据->检查持有的股票是否达到卖出、买入阀值。
 * 
 * @author zman
 *
 */
@Component
public class Scheduler {

    private static final Logger logger = LoggerFactory
            .getLogger(Scheduler.class);

    @Autowired
    private StockBasicInfoSinaDownloader StockBasicInfoSinaDownloader;
    @Autowired
    private StockCountDownloader countDownloader;
    @Autowired
    private StockMainBusinessDownloader mainBusinessDownloader;
    @Autowired
    private StockFinanceForecastDownloader financeForecastDownloader;
    @Autowired
    private StockBasicFinanceFromSinaDownloader stockBasicFinanceFromSinaDownloader;
    @Autowired
    private SelectStockByQuarterFinance quarterSelector;
    @Autowired
    private SelectStockByAnnualFinance annualSelector;
    @Autowired
    private SelectStockByBothInfo bothSelector;
    @Autowired
    private GenerateHoldStockFinanceInfo holdStockFinanceGenerator;
    @Autowired
    private StockSelectMonitor selectMonitor;

    @Autowired
    private StockHoufuquanDailyPriceHistoryDownloader priceHistoryDownloader;
    @Autowired
    private HoldStockMonitor holdStockMonitor;

    @Autowired
    private StockDataService stockDataService;

    /**
     * 每天早上六点： 下载股票基本信息，股数，主营业务
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void downloadStockBasicInfo() {
        long starttime = System.currentTimeMillis();
        logger.info("开始下载股票基本信息...");
        try {
            StockBasicInfoSinaDownloader.download();
            long usedTime = (System.currentTimeMillis() - starttime) / 1000;
            logger.info("下载基本信息结束，用时{}s", usedTime);
        } catch (IOException e) {
            logger.error("下载股票基本信息失败", e);
        }

//        已经包含在获取公司主营业务信息的逻辑中
//        starttime = System.currentTimeMillis();
//        logger.info("开始下载股票总股数...");
//        countDownloader.download();
//        long usedTime = (System.currentTimeMillis() - starttime) / 1000;
//        logger.info("下载股票总股数结束，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("开始下载公司主营业务...");
        mainBusinessDownloader.download();
        long usedTime = (System.currentTimeMillis() - starttime) / 1000;
        logger.info("下载公司主营业务结束，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("设置持有股票的当前价格...");
        try {
            updateHoldStockPrice();
            usedTime = (System.currentTimeMillis() - starttime) / 1000;
            logger.info("设置持有股票的当前价格，用时{}s", usedTime);
        }catch(Exception e){
            logger.error("设置持有股票的当前价格失败", e);
        }
    }

    private void updateHoldStockPrice() throws Exception {

        Map<String,StockBasicInfo> allStockBasicInfoMap = stockDataService.getAllStockBasicInfo();
        Map<String, HoldStockInfo> holdStockMap = stockDataService.loadHoldStockInfo();
        for( HoldStockInfo stock : holdStockMap.values()){
            try {
                stock.price = allStockBasicInfoMap.get(stock.code).price;
                if( Float.parseFloat(stock.price) > Float.parseFloat(stock.sellPrice) ){
                    stock.peAction = "卖出";
                }else if( Float.parseFloat(stock.price) < Float.parseFloat(stock.buyPrice) ){
                    stock.peAction = "买入";
                }else{
                    stock.peAction = "持有";
                }
            }catch(Exception e){
                logger.error("",e);
            }
        }
        stockDataService.writeHoldStockInfo(holdStockMap);
    }

    /**
     * 每天五点：下载股票基本财务信息->筛选股票->分析是否有变动->发送邮件 。 下载股票后复权股价历史数据->检查持有的股票是否达到卖出、买入阀值。
     */
    @Scheduled(cron = "0 0 5 * * *")
    public void downloadAndAnalysisStockFinance() {
        long starttime = System.currentTimeMillis();
        logger.info("开始下载股票基本财务信息...");
        stockBasicFinanceFromSinaDownloader.download();
        long usedTime = (System.currentTimeMillis() - starttime) / 1000;
        logger.info("下载基本财务信息结束，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("开始筛选季度优秀股票...");
        quarterSelector.select();
        usedTime = (System.currentTimeMillis() - starttime) / 1000;
        logger.info("筛选季度优秀股票结束，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("开始筛选年度优秀股票...");
        annualSelector.select();
        usedTime = (System.currentTimeMillis() - starttime) / 1000;
        logger.info("筛选年度优秀股票结束，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("开始筛选全优股票...");
        bothSelector.select();
        usedTime = (System.currentTimeMillis() - starttime) / 1000;
        logger.info("筛选全优股票结束，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("开始下载财务预告...");
        financeForecastDownloader.download();
        usedTime = (System.currentTimeMillis() - starttime) / 1000;
        logger.info("下载财务预告结束，用时{}s", usedTime);

        // 生成持股财务信息
        starttime = System.currentTimeMillis();
        try {
            holdStockFinanceGenerator.generateFinanceInfo();
        } catch (Exception e) {
            logger.error("生成持股财务信息时出错", e);
        }
        usedTime = (System.currentTimeMillis() - starttime)/1000;
        logger.info("生成持股财务报告，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("开始检查优秀股票是否有变动...");
        selectMonitor.monitor();
        usedTime = (System.currentTimeMillis() - starttime) / 1000;
        logger.info("检查优秀股票变动结束，用时{}s", usedTime);

        starttime = System.currentTimeMillis();
        logger.info("开始下载股价历史");
        try {
            priceHistoryDownloader.download();
            usedTime = (System.currentTimeMillis() - starttime) / 1000;
            logger.info("下载股价历史结束，用时{}s", usedTime);
        } catch (Exception e) {
            logger.error("下载股价历史失败", e);
        }

        starttime = System.currentTimeMillis();
        logger.info("开始检查持股是否需要变动...");
        try {
            holdStockMonitor.monitor();
            usedTime = (System.currentTimeMillis() - starttime) / 1000;
            logger.info("检查持股是否需要变动结束，用时{}s", usedTime);
        } catch (Exception e) {
            logger.error("检查持股是否需要变动失败", e);
        }

    }
}
