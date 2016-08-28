package com.zman.stock.downloader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.DownloadUtil;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 下载股票基本信息：code、name、price
 * 数据源：sina
 * Created by zman on 2016/8/28.
 */
@Service
public class StockBasicInfoSinaDownloader {
    private static final Logger logger = LoggerFactory.getLogger(StockBasicFinanceFromSinaDownloader.class);

    /**
     * 用来获取页面数量
     */
    @Value("${stock.basic.info.sina.homepage.url}")
    private String homepageUrl;
    /**
     * 获取股票基本信息
     */
    @Value("${stock.basic.info.sina.url}")
    private String baseUrl;

    @Value("${stock.basic.info.file}")
    private String filePath;

    private ObjectMapper objectMapper;

    @Autowired
    private StockDataService stockDataService;

    public StockBasicInfoSinaDownloader(){
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public void download() throws IOException {
        Map<String, StockBasicInfo> result = new HashMap<>();

        // 抓取首页信息
        int pageCount = 0;
        try {
            pageCount = findPageCount(homepageUrl);
        } catch (DownloadFailException e) {
            logger.error("抓取所有股票基本信息时，获取首页失败", e);
        }
        // 获得的股票数据
        for (int i = 1; i <= pageCount; i++) {
            try {
                Map<String, StockBasicInfo> tmp = findStockBasicInfo(String.format(
                        baseUrl, i));
                result.putAll(tmp);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        // 注入count、mainBusiness，如果有
        Collection<StockBasicInfo> tmp = stockDataService
                .getAllStockBasicInfo().values();
        tmp.forEach(info -> {
            if (result.containsKey(info.code)) {
                StockBasicInfo t = result.get(info.code);
                t.count = info.count;
                t.mainBusiness = info.mainBusiness;
            }
        });
        // 保存
        objectMapper.writeValue(new File(filePath), result);
    }

    private Map<String, StockBasicInfo> findStockBasicInfo(String url) throws DownloadFailException, IOException {
        Map<String, StockBasicInfo> result = new HashMap<>();
        // 抓取页面
        String content = DownloadUtil.downloadContent(url);

        // 获得股票信息
        JsonNode jsonNode = objectMapper.readTree(content);
        jsonNode.iterator().forEachRemaining(new Consumer<JsonNode>() {
            @Override
            public void accept(JsonNode jsonNode) {
                StockBasicInfo stock = new StockBasicInfo();
                stock.code = jsonNode.get("code").asText();
                stock.name = jsonNode.get("name").asText();
                stock.price = jsonNode.get("trade").asText();
                result.put(stock.code, stock);
                logger.debug("{}:{}:{}", stock.code, stock.name, stock.price);
            }
        });

        logger.info("下载页面->股票数量:{}", result.size());
        return result;
    }

    private int findPageCount(String homepageUrl) throws DownloadFailException, HttpStatusException {
        // 抓取页面
        String content = DownloadUtil.downloadContent(homepageUrl);
        // pageCount
        String pageCount = content.split("\"")[1];

        logger.debug("pageCount: {}", pageCount);

        return Integer.parseInt(pageCount)/80+1;  //新浪每页80个股票
    }

}
