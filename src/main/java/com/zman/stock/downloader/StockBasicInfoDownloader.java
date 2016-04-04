package com.zman.stock.downloader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.util.DownloadUtil;

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

    @Value("${stock.basic.info.url")
    private String baseUrl;

    @Value("${stock.basic.info.file}")
    private String filePath;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void download() throws IOException {
        Map<String, StockBasicInfo> result = new HashMap<>();

        // 抓取首页信息
        int pageCount = 0;
        try {
            pageCount = findFirstPage(String.format(baseUrl, 1));
        } catch (DownloadFailException e) {
            logger.error("抓取所有股票基本信息时，获取首页失败", e);
        }
        // 获得后面页面的股票数据
        for (int i = 1; i <= pageCount; i++) {
            try {
                Map<String, StockBasicInfo> tmp = findOtherPage(String.format(
                        baseUrl, i));
                result.putAll(tmp);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        // 保存
        objectMapper.writeValue(new File(filePath), result);
    }

    /**
     * 抓取首页信息，分析所有页数
     * 
     * @param url
     * @return 第一项为页面总数
     * @throws IOException
     * @throws DownloadFailException
     */
    private int findFirstPage(String url) throws DownloadFailException {

        // 抓取页面
        Document doc = DownloadUtil.downloadDoc(url);
        // 获得页面数量
        String pageInfo = doc.select("span.page_info").text();
        String pageCount = pageInfo.split("/")[1];

        logger.debug("pageCount: {}", pageCount);

        return Integer.parseInt(pageCount);
    }

    private Map<String, StockBasicInfo> findOtherPage(String url)
            throws DownloadFailException {
        Map<String, StockBasicInfo> result = new HashMap<>();
        // 抓取页面
        Document doc = DownloadUtil.downloadDoc(url);

        // 获得股票信息
        Elements trs = doc.select("tbody tr");
        trs.stream().forEach(tr -> {
            Elements tds = tr.select("td");

            StockBasicInfo stock = new StockBasicInfo();
            stock.code = tds.get(1).text();
            stock.name = tds.get(2).text();
            stock.price = tds.get(3).text();
            result.put(stock.code, stock);
            logger.debug("{}:{}:{}", stock.code, stock.name, stock.price);
        });

        logger.info("下载页面->股票数量:{}", result.size());
        return result;
    }
}
