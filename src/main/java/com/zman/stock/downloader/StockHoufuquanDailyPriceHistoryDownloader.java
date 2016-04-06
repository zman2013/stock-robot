package com.zman.stock.downloader;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.DownloadUtil;

/**
 * 下载后复权日级股价历史数据，并进行分析，然后更新持有股票信息
 * 
 * @author zman
 *
 */
@Service
public class StockHoufuquanDailyPriceHistoryDownloader {

    @Value("${stock.houfuquan.price.url}")
    private String houfuquanDailyPriceUrl;
    @Autowired
    private StockDataService stockDataService;

    private static final Pattern pattern = Pattern
            .compile("_(\\d+_\\d+_\\d+):\"(.*?)\",");

    public void download() throws Exception {

        Map<String, HoldStockInfo> holdStockMap = stockDataService
                .loadHoldStockInfo();

        // 计算股票指标
        for (HoldStockInfo stock : holdStockMap.values()) {
            // 下载
            String content = downloadPriceAndOutput(houfuquanDailyPriceUrl,
                    stock.code);

            // 提取4大股价
            double[] priceArray = findPrices(content, stock.date);

            double holdPrice = priceArray[0];
            double maxPrice = priceArray[1];
            double minPrice = priceArray[2];
            double currentPrice = priceArray[3];

            // 分析四大股价
            // 从最高点跌了9%
            if ((currentPrice - maxPrice) / maxPrice < -0.09) {
                stock.action = "卖出";
                // 从最低点涨12%
            } else if ((currentPrice - minPrice) / minPrice > 0.1) {
                stock.action = "买入";
            } else {
                stock.action = "持有";
            }
            // 持有至今收益
            stock.profit = String.format("%.2f", (currentPrice - holdPrice)
                    / holdPrice * 100);
        }

        // 写入文件
        stockDataService.writeHoldStockInfo(holdStockMap);
    }

    /**
     * 0 holdPrice = 0d; 1 maxPrice = 0d; 2 minPrice = 0d; 3 currentPrice = 0d;
     * 
     * 
     * @param content
     * @param holdDate
     * @return
     */
    private double[] findPrices(String content, String holdDate) {
        double[] priceArray = new double[] { 0d, 0d, 10000d, 0d };
        Matcher matcher = pattern.matcher(content);
        boolean first = true;
        while (matcher.find()) {
            String date = matcher.group(1).replaceAll("_", "-");
            double price = Double.parseDouble(matcher.group(2));

            if (date.compareTo(holdDate) == 0) {
                priceArray[0] = price;
            }
            if (date.compareTo(holdDate) >= 0) {
                if (price > priceArray[1]) {
                    priceArray[1] = price;
                }
                if (price < priceArray[2]) {
                    priceArray[2] = price;
                }
                if (first) {
                    priceArray[3] = price; // 第一条数据为最新股价
                    first = false;
                }
            }
        }
        return priceArray;
    }

    private String downloadPriceAndOutput(String baseUrl, String code) {

        // 由于不知道股票是上证，还是深证，于是拼上sh或者sz，分别请求。忽略请求失败的。

        try {
            String content = DownloadUtil.downloadContent(String.format(
                    baseUrl, "sz" + code));
            return content;
        } catch (DownloadFailException e) {

        } catch (HttpStatusException e) {
        }

        try {
            String content = DownloadUtil.downloadContent(String.format(
                    baseUrl, "sh" + code));
            return content;
        } catch (DownloadFailException e) {
        } catch (HttpStatusException e) {
        }

        return "";
    }
}
