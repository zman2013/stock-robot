package com.zman.stock.downloader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.zman.stock.data.domain.HoldStockInfo;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.util.DownloadUtil;

/**
 * 股票股数下载，股数单位是：万股
 * 
 * @author zman
 *
 */
@Service
public class StockCountDownloader extends AbstractLoopAllStockDownloader {

    private final static Logger logger = LoggerFactory
            .getLogger(StockCountDownloader.class);

    @Value("${stock.count.url}")
    private String baseUrl;

    @Override
    public void download() {
        // 加载所有股票基本信息
        Map<String, StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo();
        // 加载所有持有的股票，有可能所有的股票基本信息中没有包含持有的股票（因为网站的数据可能不全）
        addHoldStockInfo(allStock);

        // 遍历并处理所有股票
        int processedCount = 0;
        for (StockBasicInfo stock : allStock.values()) {

            try {
                // 下载页面,并处理
                Map<String, String> result = process(String.format(baseUrl, stock.code));
                // 保存信息
                long count = (long) (Double.parseDouble((String) result.get(("count"))) * 10000);
                stock.count = count;
            } catch (Exception e) {
                logger.error("下载总股数失败，stock:" + stock.code, e);
            }

            if (processedCount++ % 100 == 0) {
                System.out.println();
            }
            System.out.print(".");

        }

        // 保存
        try {
            mapper.writeValue(new File(stockBasicInfoFile), allStock);
        } catch (IOException e) {
            logger.error("写入股票基本信息文件时出错", e);
        }
    }

    /**
     * 尝试插入持有的股票信息到所有股票信息中，防止所有股票不包含持有的股票信息
     * 因为网站的数据可能不全
     * @param allStock
     */
    private void addHoldStockInfo(Map<String, StockBasicInfo> allStock) {
        try {
            Map<String, HoldStockInfo> stockMap = stockDataService
                    .loadHoldStockInfo();
            for( Map.Entry<String,HoldStockInfo> entry : stockMap.entrySet() ){
                String code = entry.getKey();
                String name = entry.getValue().name;
                if( !allStock.containsKey(code) ){
                    StockBasicInfo stock = new StockBasicInfo();
                    stock.code = code;
                    stock.name = name;
                    stock.price = "0";
                    allStock.put(code, stock);
                }
            }
        } catch (Exception e) {
            logger.error("加载持有的股票信息出错",e);
        }
    }

    @Override
    protected String getFilePath(String code) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param url
     * @return "count" -> count, 例: "count" -> 10000
     * @throws DownloadFailException
     */
    @Override
    protected Map<String, String> process(String url)
            throws DownloadFailException {
        Document doc = DownloadUtil.downloadDoc(url);
        //总股本结构 部分，提取最新季度 总股本数
        Elements elements = doc.select("#stockcapit.gqtz tbody tr td:eq(1)");
        String count = elements.get(0).text();

        Map<String,String> map = new HashMap<>();
        map.put("count",count);
        return map;
    }
}
