package com.zman.stock.service;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;

@Service
public class StockDataService {

    private static final Logger logger = LoggerFactory
            .getLogger(StockDataService.class);

    protected final static ObjectMapper mapper = new ObjectMapper();

    @Value("${stock.basic.info.file}")
    protected String stockBasicInfoFile;

    @Value("${stock.basic.finance.dir}")
    private String basicFinanceDir;

    private Map<String, StockBasicInfo> allStockBasicInfoMap = null;

    /**
     * 读取文件获得所有股票基本信息
     * 
     * @return
     */
    public Collection<StockBasicInfo> getAllStockBasicInfo() {
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
        return allStockBasicInfoMap.values();
    }

    /**
     * 获得股票的财务数据：报告期->指标->数值
     * 
     * 报告期，例： "2015年年报", "2015年三季报", "2015年中报", "2015年一季报", "2014年年报"
     * 
     * 指标：发布日期，营业收入，收入同比增长率，收入环比增长率，净利润，净利润同比增长率"，净利润环比增长率",
     * ，每股收益，每股净资产，资产收益率，每股现金流，毛利率
     * 
     * @param stockCode
     * @return
     * @throws Exception
     */
    public Map<String, Map<String, String>> getBasicFinanceData(String stockCode)
            throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> financeMap = mapper.readValue(
                new File(basicFinanceDir, stockCode), Map.class);
        return financeMap;
    }

}
