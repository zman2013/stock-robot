package com.zman.stock.downloader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.DownloadFailException;

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
        Collection<StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo();

        // 遍历并处理所有股票
        int processedCount = 0;
        for (StockBasicInfo stock : allStock) {

            try {
                // 下载页面,并处理
                Map<String, Object> result = process(stock.code);
                // 保存信息
                String mainBusinessInfo = (String) result.get(stock.code);
                stock.mainBusiness = mainBusinessInfo;
            } catch (Exception e) {
                logger.error("", e);
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
     * @param baseUrl
     * @param code
     * @param name
     * @return code -> mainBusiness, 例: 000848 -> 露露产品
     * @throws DownloadFailException
     * @throws IOException
     */
    @Override
    protected Map<String, Object> process(String code)
            throws DownloadFailException, IOException {
        Map<String, Object> result = new HashMap<>();
        try {
            Document doc = Jsoup.connect(String.format(baseUrl, "sz" + code))
                    .get();
            Elements eles = doc.select("div.com_overview p:eq(3)");
            String mainBusiness = eles.text();
            result.put(code, mainBusiness);
            return result;
        } catch (HttpStatusException e) {
        }

        try {
            Document doc = Jsoup.connect(String.format(baseUrl, "sh" + code))
                    .get();
            Elements eles = doc.select("div.com_overview p:eq(3)");
            String mainBusiness = eles.text();
            result.put(code, mainBusiness);
        } catch (HttpStatusException e) {
        }

        return result;
    }
}
