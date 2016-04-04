package com.zman.stock.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.downloader.StockBasicInfoDownloader;
import com.zman.stock.service.StockBasicInfoService;

@Service
public class StockBasicInfoServiceImpl implements StockBasicInfoService {

    private static final Logger logger = LoggerFactory
            .getLogger(StockBasicInfoServiceImpl.class);

    @Value("${stock.basic.info.file}")
    private String filePath;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StockBasicInfoDownloader basicInfoDownloader;

    @Override
    public void run() {
        try {
            logger.info("开始下载股票基本信息...");
            long time = System.currentTimeMillis();
            Map<String, StockBasicInfo> map = basicInfoDownloader.download();
            logger.info("股票基本信息下载完毕,用时:{}s",
                    (System.currentTimeMillis() - time) / 1000.0);

            logger.info("开始将股票基本信息存入文件...");
            time = System.currentTimeMillis();
            objectMapper.writeValue(new File(filePath), map);
            logger.info("存储股票基本信息完毕,用时:{}s",
                    (System.currentTimeMillis() - time) / 1000.0);
        } catch (IOException e) {
            logger.error("下载股票基本信息时出错", e);
        }
    }
}
