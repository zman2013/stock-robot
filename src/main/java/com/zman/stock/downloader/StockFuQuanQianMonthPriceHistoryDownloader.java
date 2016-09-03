package com.zman.stock.downloader;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.StockPrice;
import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.util.DownloadUtil;

/**
 * 获得复权前的股票价格，月级数据（最高价、最低价），用于计算pe
 * 数据源：同花顺
 * @author zman
 *
 */
@Service
public class StockFuQuanQianMonthPriceHistoryDownloader {

    private final static Logger logger = LogManager
            .getLogger(StockFuQuanQianMonthPriceHistoryDownloader.class);

    public List<StockPrice> download(String code, float currentPrice) {

//        String baseUrl = "http://d.10jqka.com.cn/v2/line/hs_%s/21/last.js";
        String shBaseUrl = "http://finance.sina.com.cn/realstock/newcompany/sh%s/pqfq.js?_=20";
        String szBaseUrl = "http://finance.sina.com.cn/realstock/newcompany/sz%s/pqfq.js?_=20";

        List<StockPrice> stockPriceList = null;

        try {
            stockPriceList = downloadPrice(String.format(shBaseUrl, code),currentPrice);
        } catch (DownloadFailException | HttpStatusException e) {
            try{
                stockPriceList = downloadPrice(String.format(szBaseUrl, code),currentPrice);
            }catch(DownloadFailException | HttpStatusException e2){
                logger.error("download stock price failed: "+szBaseUrl+" "+code,e2);
                stockPriceList = Collections.emptyList();
            }
        }

        return stockPriceList;
    }

    // 20031219,0.79,0.85,0.55,0.58,14108237,271276730.00,1065.746;
    private static Pattern pattern = Pattern
            .compile("_(\\d{4})_(\\d{2})_(\\d{2}):\"([\\-\\.\\d]+)\",.*?");

    /**
     * 
     * @return
     * @throws DownloadFailException
     * @throws HttpStatusException
     */
    private List<StockPrice> downloadPrice(String url, float currentPrice)
            throws DownloadFailException, HttpStatusException {

        String content = DownloadUtil.downloadContent(url);
        Map<String,StockPrice> stockPriceMap = new HashMap<>();

        List<StockPrice> stockPriceList = new LinkedList<>();

        float ratio = -1;

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String year = matcher.group(1);
            String month = matcher.group(2);
            String day = matcher.group(3);
            String ym = year+month;
            float price = Float.parseFloat(matcher.group(4));

            if( ratio == -1 ){   //ratio==-1表示匹配到的第一条数据，即：复权前最新股价
                ratio = currentPrice / price;
                price = currentPrice;
            }else{
                price = price * ratio;
            }

            if( stockPriceMap.containsKey(ym) ){
                StockPrice p = stockPriceMap.get(ym);
                p.maxPrice = p.maxPrice < price?price:p.maxPrice;
                p.minPrice = p.minPrice > price?price:p.minPrice;
            }else{
                StockPrice p = new StockPrice();
                p.date = ym+day;
                p.maxPrice = price;
                p.minPrice = price;
                stockPriceMap.put(ym, p);
                stockPriceList.add(p);
            }
        }

        return stockPriceList;
    }
}
