package com.zman.stock.downloader;

import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.DownloadFailException;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 股票股数下载，股数单位是：万股
 * 
 * @author zman
 *
 */
@Deprecated
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
                Map<String, String> result = process(String.format(baseUrl, "sh"+stock.code));
                // 保存信息
                String countString = result.get("count");
                double count = Double.parseDouble(countString) * 10000;
                stock.count = (long) count;
            } catch (Exception e) {
                try {
                    // 下载页面,并处理
                    Map<String, String> result = process(String.format(baseUrl, "sz" + stock.code));
                    // 保存信息
                    String countString = result.get("count");
                    double count = Double.parseDouble(countString) * 10000;
                    stock.count = (long) count;
                }catch (Exception e2){
                    logger.error("获取股票总股数失败, "+stock.code + " " + baseUrl );
                }
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

    private static Pattern pattern = Pattern.compile("var totalcapital = (\\d+\\.?\\d*);");
            //.compile("_(\\d{4})_(\\d{2})_(\\d{2}):\"([\\-\\.\\d]+)\",.*?");

    /**
     * @param url
     * @return "count" -> count, 例: "count" -> 10000
     * @throws DownloadFailException
     */
    @Override
    protected Map<String, String> process(String url) throws DownloadFailException, HttpStatusException {
//        String content = DownloadUtil.downloadContent(url);
//        Matcher matcher = pattern.matcher(content);
//        if(matcher.find()){
//            String count = matcher.group(1);
//            Map<String,String> map = new HashMap<>();
//            map.put("count",count);
//            return map;
//        }

        throw new RuntimeException("在页面中未找到股数信息");

    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
