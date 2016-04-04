package com.zman.stock.service.impl;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zman.stock.data.dao.StockMapper;
import com.zman.stock.data.domain.Stock;
import com.zman.stock.downloader.StockBasicInfoDownloader;
import com.zman.stock.service.StockBasicInfoService;

@Service
public class StockBasicInfoServiceImpl implements StockBasicInfoService {

    private static final Logger logger = LoggerFactory
            .getLogger(StockBasicInfoServiceImpl.class);

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockBasicInfoDownloader basicInfoDownloader;

    @Override
    public void run() {
        try {
            logger.info("开始下载股票基本信息...");
            long time = System.currentTimeMillis();
            List<Stock> stockList = basicInfoDownloader.download();
            logger.info("股票基本信息下载完毕,用时:{}", System.currentTimeMillis() - time);

            logger.info("开始将股票基本信息存入数据库 ...");
            time = System.currentTimeMillis();
            stockList.forEach(stock -> {
                Stock tmp = stockMapper.selectByPrimaryKey(stock.getCode());
                if (tmp != null) {
                    stockMapper.updateByPrimaryKeySelective(stock);
                } else {
                    stockMapper.insertSelective(stock);
                }
            });
            logger.info("存储股票基本信息完毕,用时:{}", System.currentTimeMillis() - time);
        } catch (IOException e) {
            logger.error("下载股票基本信息时出错", e);
        }
    }
}
