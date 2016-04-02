package com.zman.stock.downloader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.Constants;

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

    private static final ObjectMapper mapper = new ObjectMapper();

    public void download() throws IOException {

        String baseUrl = "http://data.10jqka.com.cn/funds/ggzjl/field/zdf/order/desc/page/%d/ajax/1/";
        // 抓取首页信息
        Map<String, Map<String, String>> map = findFirstPage(String.format(
                baseUrl, 1));
        int stockCount = Integer.parseInt(map.get("count").get("count"));
        map.remove("count");
        // 获得后面页面的股票数据
        for (int i = 2; i <= stockCount; i++) {
            Map<String, Map<String, String>> tmp = findOtherPage(String.format(
                    baseUrl, i));
            map.putAll(tmp);
        }
        // 输出到文件
        mapper.writeValue(new File(Constants.allStockPath), map);

    }

    /**
     * 抓取首页信息，分析所有页数和首页的所有股票信息
     * 
     * @param url
     * @return 第一项为页面总数，后面的为股票1编码，股票1名称，股票2编码，股票2名称。。。
     * @throws IOException
     */
    private Map<String, Map<String, String>> findFirstPage(String url)
            throws IOException {
        Map<String, Map<String, String>> map = new HashMap<>();

        // 抓取页面
        Document doc = Jsoup.connect(url).get();

        // 获得页面数量
        String pageInfo = doc.select("span.page_info").text();
        String pageCount = pageInfo.split("/")[1];
        logger.debug("pageCount: {}", pageCount);
        Map<String, String> countMap = new HashMap<>();
        countMap.put("count", pageCount);
        map.put("count", countMap);

        // 获得股票信息
        Elements trs = doc.select("tbody tr");
        trs.stream().forEach(tr -> {
            Elements tds = tr.select("td");
            String code = tds.get(1).text();
            String name = tds.get(2).text();
            String price = tds.get(3).text();
            Map<String, String> tmp = new HashMap<>();
            tmp.put("name", name);
            tmp.put("price", price);
            map.put(code, tmp);
            logger.debug("{}:{}:{}", code, name, price);
        });

        return map;
    }

    private Map<String, Map<String, String>> findOtherPage(String url)
            throws IOException {
        Map<String, Map<String, String>> map = new HashMap<>();

        // 抓取页面
        Document doc = Jsoup.connect(url).get();

        // 获得股票信息
        Elements trs = doc.select("tbody tr");
        trs.stream().forEach(tr -> {
            Elements tds = tr.select("td");
            String code = tds.get(1).text();
            String name = tds.get(2).text();
            String price = tds.get(3).text();
            Map<String, String> tmp = new HashMap<>();
            tmp.put("name", name);
            tmp.put("price", price);
            map.put(code, tmp);
            logger.debug("{}:{}:{}", code, name, price);
        });

        return map;
    }
}
