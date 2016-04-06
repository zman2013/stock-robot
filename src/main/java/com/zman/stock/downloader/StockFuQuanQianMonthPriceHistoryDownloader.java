package com.zman.stock.downloader;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
 * 
 * @author zman
 *
 */
@Service
public class StockFuQuanQianMonthPriceHistoryDownloader {

    private final static Logger logger = LogManager
            .getLogger(StockFuQuanQianMonthPriceHistoryDownloader.class);

    public List<StockPrice> download(String code) {

        String baseUrl = "http://d.10jqka.com.cn/v2/line/hs_%s/21/last.js";

        List<StockPrice> stockPriceList = null;

        try {
            stockPriceList = downloadPrice(String.format(baseUrl, code));
        } catch (DownloadFailException | HttpStatusException e) {
            logger.error(e.getMessage(), e);
            stockPriceList = Collections.emptyList();
        }

        return stockPriceList;
    }

    // 20031219,0.79,0.85,0.55,0.58,14108237,271276730.00,1065.746;
    private static Pattern pattern = Pattern
            .compile("(\\d+),[\\-\\.\\d]+,([\\-\\.\\d]+),([\\-\\.\\d]+),.*?");

    /**
     * 
     * @param baseUrl
     * @param code
     * @return
     * @throws DownloadFailException
     * @throws HttpStatusException
     */
    private List<StockPrice> downloadPrice(String url)
            throws DownloadFailException, HttpStatusException {

        String content = DownloadUtil.downloadContent(url);

        List<StockPrice> stockPriceList = new LinkedList<>();

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            StockPrice p = new StockPrice();
            p.date = matcher.group(1);
            p.maxPrice = Float.parseFloat(matcher.group(2));
            p.minPrice = Float.parseFloat(matcher.group(3));
            stockPriceList.add(p);
        }

        return stockPriceList;
    }
}
