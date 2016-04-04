package com.zman.stock.downloader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.service.domain.DownloadFailException;

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

    public void download() {
        // 加载所有股票基本信息
        Map<String, StockBasicInfo> allStockMap = loadAllStockBasicInfo();

        // 遍历并处理所有股票
        int processedCount = 0;
        for (Entry<String, StockBasicInfo> stock : allStockMap.entrySet()) {

            String code = stock.getKey();

            try {
                // 下载页面,并处理
                Map<String, Object> result = process(code);
                // 保存信息
                String filePath = getFilePath(code);
                mapper.writeValue(new File(filePath), result);
            } catch (Exception e) {
                logger.error("", e);
            }

            if (processedCount++ % 100 == 0) {
                System.out.println();
            }
            System.out.print(".");
        }

    }

    /**
     * 加载所有股票的基本信息
     * 
     * @return
     */
    protected Map<String, StockBasicInfo> loadAllStockBasicInfo() {
        Map<String, StockBasicInfo> allStockBasicInfoMap = null;
        // 读取所有股票基本信息
        try {
            JavaType javaType = mapper.getTypeFactory().constructMapType(
                    Map.class, String.class, StockBasicInfo.class);
            allStockBasicInfoMap = mapper.readValue(
                    new File(stockBasicInfoFile), javaType);
        } catch (Exception e) {
            logger.error("从文件中读取所有股票信息出错,file:{}", stockBasicInfoFile);
            logger.error("", e);
            allStockBasicInfoMap = Collections.emptyMap();
        }
        return allStockBasicInfoMap;
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
    protected abstract Map<String, Object> process(String code)
            throws DownloadFailException, IOException;
}
