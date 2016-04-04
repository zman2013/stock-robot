package com.zman.stock.downloader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.service.domain.DownloadFailException;
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

    public Map<String, StockBasicInfo> download() throws IOException {
        Map<String, StockBasicInfo> result = new HashMap<>();

        String baseUrl = "http://data.10jqka.com.cn/funds/ggzjl/field/zdf/order/desc/page/%d/ajax/1/";
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
        // 返回
        return result;
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
            String code = tds.get(1).text();
            String name = tds.get(2).text();
            StockBasicInfo stock = new StockBasicInfo();
            stock.code = code;
            stock.name = name;
            result.put(code, stock);
            logger.debug("{}:{}", code, name);
        });

        logger.info("下载页面->股票数量:{}", result.size());
        return result;
    }
}
