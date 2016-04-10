package com.zman.stock.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.selector.SelectStockData;

/**
 * 加载股票删选结果
 * 
 * @author zman
 *
 */
@Service
public class LoadSelectStockService {

    private static final Logger logger = LoggerFactory
            .getLogger(LoadSelectStockService.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${stock.select.by.annual.finance.file}")
    private String annualFilepath;

    @Value("${stock.select.by.quarter.finance.file}")
    private String quarterFilepath;

    @Value("${stock.select.by.both.finance.file}")
    private String bothFilepath;
    @Value("${stock.hold.finance.file}")
    private String stockHoldFinanceFile;

    /**
     * 加载按季度财务信息删选出的结果
     * 
     * @param model
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<SelectStockData> loadQuarter() throws Exception {
        return loadSelectedStockData(quarterFilepath);
    }

    public List<SelectStockData> loadQuarterBackup() throws Exception {
        return loadSelectedStockData(quarterFilepath
                + StockDataService.backupExtension);
    }

    /**
     * 加载按年度财务信息删选出的结果
     * 
     * @param model
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<SelectStockData> loadAnnual() throws Exception {
        return loadSelectedStockData(annualFilepath);
    }

    public List<SelectStockData> loadAnnualBackup() throws Exception {
        return loadSelectedStockData(annualFilepath
                + StockDataService.backupExtension);
    }

    /**
     * 取季度筛选和年度筛选的交集
     * 
     * @param model
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<SelectStockData> loadBoth() throws Exception {
        return loadSelectedStockData(bothFilepath);
    }

    public List<SelectStockData> loadBothBackup() throws Exception {
        return loadSelectedStockData(bothFilepath
                + StockDataService.backupExtension);
    }

    public List<SelectStockData> loadHoldStockData() throws Exception {
        return loadSelectedStockData(stockHoldFinanceFile);
    }

    public List<SelectStockData> loadSelectedStockData(String filepath)
            throws Exception {
        File file = new File(filepath);
        if (!file.exists()) {
            logger.warn("文件没找到:{}", filepath);
            return Collections.emptyList();
        }

        List<SelectStockData> stockDataList = mapper.readValue(file,
                new TypeReference<List<SelectStockData>>() {
                });

        return stockDataList;
    }
}
