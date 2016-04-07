package com.zman.stock.selector;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.StockDataTools;

/**
 * 以ChooseStockByAnnualFinance和ChooseStockByQuarterFinance的输出为输入， 得到两者的交集
 * 
 * @author zman
 *
 */
@Service
public class SelectStockByBothInfo {

    private static final Logger logger = LoggerFactory
            .getLogger(SelectStockByBothInfo.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${stock.select.by.annual.finance.file}")
    private String annualFilepath;
    @Value("${stock.select.by.quarter.finance.file}")
    private String quarterFilepath;
    @Value("${stock.select.by.both.finance.file}")
    private String bothFilepath;

    @Autowired
    private StockDataService stockDataService;

    public void select() {
        try {
            selectImpl();
        } catch (IOException e) {
            logger.error("根据季度和年度分析结果筛选股票遇到错误", e);
        }
    }

    private void selectImpl() throws JsonParseException, JsonMappingException,
            IOException {
        Set<SelectStockData> annualData = mapper.readValue(new File(
                annualFilepath), new TypeReference<Set<SelectStockData>>() {
        });
        Set<SelectStockData> quarterData = mapper.readValue(new File(
                quarterFilepath), new TypeReference<Set<SelectStockData>>() {
        });

        Set<SelectStockData> finalData = StockDataTools
                .createSortedSetForStockData();
        quarterData.forEach(stock -> {
            if (annualData.contains(stock)) {
                finalData.add(stock);
            }
        });

        // json格式
        stockDataService.backupAndWriteNew(bothFilepath, finalData);
    }
}
