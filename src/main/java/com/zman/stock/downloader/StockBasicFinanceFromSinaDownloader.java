package com.zman.stock.downloader;

import com.zman.stock.data.domain.StockFinanceBO;
import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.util.DownloadUtil;
import com.zman.stock.util.FinanceTools;
import com.zman.stock.util.StockDataTools;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * 下载股票的基本财务信息
 * 此数据来自新浪
 * @author zman
 *
 */
@Service
public class StockBasicFinanceFromSinaDownloader extends AbstractLoopAllStockDownloader {

    private static final Logger logger = LoggerFactory.getLogger(StockBasicFinanceFromSinaDownloader.class);

    @Value("${stock.basic.finance.url}")
    private String baseUrl;

    @Value("${stock.basic.finance.dir}")
    private String baseDir;

    @Autowired
    private StockDetailedFinanceDownloader detailedFinanceDownloader;

    @Override
    protected String getFilePath(String code) {
        return baseDir + code;
    }

    @Override
    protected Map<String, ?> process(String code) throws DownloadFailException,
            IOException {
        Map<String, Map<String, String>> finance = new HashMap<>();
        try {
            List<StockFinanceBO> stockFinanceBOList = detailedFinanceDownloader.findByStockList(Arrays.asList(code), "ProfitStatement");
            stockFinanceBOList.forEach( f -> {
                Map<String,Map<String,Float>> data = f.getData();
                Map<String,Float> revenueMap = data.get("一、营业总收入");
                Map<String,Float> profitMap = data.get("四、净利润");
                Set<String> dateSet = revenueMap.keySet();
                String revenueItem = "一、营业总收入";
                String profitItem = "四、净利润";
                dateSet.forEach( date -> {
                    try {
                        String version = FinanceTools.convertToVersion(date);

                        //计算收入数据
                        Float currentRevenue = revenueMap.get(date); //当期收入
                        String revenueYearGrowth = FinanceTools.computeYearGrowth(data, revenueItem, date); //同比增长率
                        String revenueSeasonGrowth = FinanceTools.computeSeasonGrowth(data, revenueItem, date); //环比增长率
                        //计算净利润数据
                        Float currentProfit = profitMap.get(date); //当期净利润
                        String profitYearGrowth = FinanceTools.computeYearGrowth(data, profitItem, date); //同比增长
                        String profitSeasonGrowth = FinanceTools.computeSeasonGrowth(data, profitItem, date); //环比增长


                        Map<String, String> tmp = new HashMap<>();
                        tmp.put("营业收入", String.format("%.0f", currentRevenue));
                        tmp.put("收入同比增长率", revenueYearGrowth);
                        tmp.put("收入环比增长率", revenueSeasonGrowth);
                        tmp.put("净利润", String.format("%.0f", currentProfit));
                        tmp.put("净利润同比增长率", profitYearGrowth);
                        tmp.put("净利润环比增长率", profitSeasonGrowth);

                        finance.put(version, tmp);
                    }catch(Exception e){
                        //ignore
                    }
                });

            });
        } catch (Exception e) {
            logger.error("处理新浪利润表数据时遇到错误,stock:{}", code);
        }
        return finance;
    }


}
