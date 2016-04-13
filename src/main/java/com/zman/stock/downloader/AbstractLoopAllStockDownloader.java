package com.zman.stock.downloader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.service.StockDataService;

/**
 * 遍历所有股票，进行下载
 * 
 * @author zman
 *
 */
public abstract class AbstractLoopAllStockDownloader {

    private static final Logger logger = LoggerFactory
            .getLogger(AbstractLoopAllStockDownloader.class);

    protected final static ObjectMapper mapper = new ObjectMapper();

    @Value("${stock.basic.info.file}")
    protected String stockBasicInfoFile;

    @Autowired
    protected StockDataService stockDataService;

    public void download(String code) {
        try {
            // 下载页面,并处理
            Map<String, ?> result = process(code);
            // 保存信息
            String filePath = getFilePath(code);
            mapper.writeValue(new File(filePath), result);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public void download() {
        // 加载所有股票基本信息
        Map<String, StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo();

        // 遍历并处理所有股票
        int processedCount = 0;
        for (StockBasicInfo stock : allStock.values()) {

            download(stock.code);

            if (processedCount++ % 100 == 0) {
                System.out.println();
            }
            System.out.print(".");
        }

    }

    /**
     * 获得股票信息的存储文件路径
     * 
     * @param code
     * @return
     */
    protected abstract String getFilePath(String code);

    /**
     * 下载文件，并提取 数据
     * 
     * @param code
     * @param name
     * @return
     * @throws DownloadFailException
     * @throws IOException
     */
    protected abstract Map<String, ?> process(String code)
            throws DownloadFailException, IOException;
}
