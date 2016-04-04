package com.zman.stock.downloader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.Stock;
import com.zman.stock.service.domain.StockWrapper;

/**
 * 下载所有股票的编号和名称
 * 
 * @author zman
 *
 */
@Service
public class StockBasicInfoDownloader {
    private static final Logger logger = LoggerFactory
            .getLogger(StockBasicInfoDownloader.class);

    public List<Stock> download() throws IOException {
        List<Stock> stockList = new LinkedList<>();

        String baseUrl = "http://data.10jqka.com.cn/funds/ggzjl/field/zdf/order/desc/page/%d/ajax/1/";
        // 抓取首页信息
        StockWrapper stockWrapper = findFirstPage(String.format(baseUrl, 1));
        stockList.addAll(stockWrapper.stockList);
        // 获得后面页面的股票数据
        for (int i = 2; i <= stockWrapper.pageCount; i++) {
            try {
                List<Stock> tmp = findOtherPage(String.format(baseUrl, i));
                stockList.addAll(tmp);
            } catch (Exception e) {
                logger.error("下载第{}页面时出错", i);
                logger.error("", e);
            }
        }
        // 返回
        return stockList;
    }

    /**
     * 抓取首页信息，分析所有页数和首页的所有股票信息
     * 
     * @param url
     * @return 第一项为页面总数，后面的为股票1编码，股票1名称，股票2编码，股票2名称。。。
     * @throws IOException
     */
    private StockWrapper findFirstPage(String url) throws IOException {

        StockWrapper stockWrapper = new StockWrapper();
        // 抓取页面
        Document doc = null;
        // 如果失败，重试三次
        int i = 0;
        while (i < 3) {
            try {
                doc = Jsoup.connect(url).get();
                break;
            } catch (Exception e) {
                i++;
                if (i >= 3) {
                    throw e;
                }
            }
        }
        // 获得页面数量
        String pageInfo = doc.select("span.page_info").text();
        String pageCount = pageInfo.split("/")[1];
        logger.debug("pageCount: {}", pageCount);
        stockWrapper.pageCount = Integer.parseInt(pageCount);

        // 获得股票信息
        Elements trs = doc.select("tbody tr");
        trs.stream().forEach(tr -> {
            Elements tds = tr.select("td");
            String code = tds.get(1).text();
            String name = tds.get(2).text();
            String price = tds.get(3).text();
            Stock stock = new Stock();
            stock.setCode(code);
            stock.setName(name);
            stock.setPrice(new BigDecimal(price));
            stockWrapper.stockList.add(stock);
            logger.debug("{}:{}:{}", code, name, price);
        });

        logger.info("下载首页完成->页面数:{},股票数:{}", stockWrapper.pageCount,
                stockWrapper.stockList.size());
        return stockWrapper;
    }

    private List<Stock> findOtherPage(String url) throws IOException {
        List<Stock> stockList = new LinkedList<>();
        // 抓取页面
        Document doc = null;
        // 如果失败，重试三次
        int i = 0;
        while (i < 3) {
            try {
                doc = Jsoup.connect(url).get();
                break;
            } catch (Exception e) {
                i++;
                if (i >= 3) {
                    throw e;
                }
            }
        }

        // 获得股票信息
        Elements trs = doc.select("tbody tr");
        trs.stream().forEach(tr -> {
            Elements tds = tr.select("td");
            String code = tds.get(1).text();
            String name = tds.get(2).text();
            String price = tds.get(3).text();
            Stock stock = new Stock();
            stock.setName(name);
            stock.setCode(code);
            stock.setPrice(new BigDecimal(price));
            stockList.add(stock);
            logger.debug("{}:{}:{}", code, name, price);
        });

        logger.info("下载页面->股票数量:{}", stockList.size());
        return stockList;
    }
}
