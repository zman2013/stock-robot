package com.zman.stock.downloader;

import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.DownloadFailException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公司的主营业务下载，存入股票的基本信息中
 * 
 * @author zman
 *
 */
@Service
public class StockMainBusinessDownloader extends AbstractLoopAllStockDownloader {

    private final static Logger logger = LoggerFactory
            .getLogger(StockMainBusinessDownloader.class);

    @Value("${stock.main.business.url}")
    private String baseUrl;

    @Override
    public void download() {
        // 加载所有股票基本信息
        Map<String, StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo();

        // 遍历并处理所有股票
        int processedCount = 0;
        for (StockBasicInfo stock : allStock.values()) {

            try {
                // 下载页面,并处理
                Map<String, String> result = process(stock.code);
                // 保存信息
                String mainBusinessInfo = (String) result.get("mainBusiness");
                stock.mainBusiness = mainBusinessInfo;

                long count = (long) (Double.parseDouble(result.get("count")) * 10000);
                stock.count = count;

            } catch (Exception e) {
                logger.error("下载主营业务失败，stock:" + stock.code, e);
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

    @Override
    protected String getFilePath(String code) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @param code
     * @return code -> mainBusiness, 例: 000848 -> 露露产品
     * @throws DownloadFailException
     * @throws IOException
     */
    @Override
    protected Map<String, String> process(String code)
            throws DownloadFailException, IOException {
        Map<String, String> result = new HashMap<>();
        try {
            Document doc = Jsoup.connect(String.format(baseUrl, "sz" + code))
                    .get();
            Elements eles = doc.select("div.com_overview p:eq(3)");
            String mainBusiness = eles.text();
            result.put("mainBusiness", mainBusiness);

            //提取总股数
            String count = extractStockCount( doc.html() );
            result.put("count", count);

            return result;
        } catch (HttpStatusException e) {
        }

        try {
            Document doc = Jsoup.connect(String.format(baseUrl, "sh" + code))
                    .get();
            Elements eles = doc.select("div.com_overview p:eq(3)");
            String mainBusiness = eles.text();
            result.put("mainBusiness", mainBusiness);

            //提取总股数
            String count = extractStockCount( doc.html() );
            result.put("count", count);

        } catch (HttpStatusException e) {
        }



        return result;
    }


    private static Pattern pattern = Pattern.compile("var totalcapital = (\\d+\\.?\\d*);");
    //.compile("_(\\d{4})_(\\d{2})_(\\d{2}):\"([\\-\\.\\d]+)\",.*?");

    /**
     * @param content
     * @return "count" -> count, 例: "count" -> 10000
     */
    protected String extractStockCount(String content) {
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            String count = matcher.group(1);
            return count;
        }

        throw new RuntimeException("在页面中未找到股数信息");

    }


}
