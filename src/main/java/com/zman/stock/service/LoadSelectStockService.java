package com.zman.stock.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${stock.select.by.annual.finance.file}")
    private String annualFilepath;

    @Value("${stock.select.by.quarter.finance.file}")
    private String quarterFilepath;

    @Value("${stock.select.by.both.file")
    private String bothFilepath;

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
        List<SelectStockData> quarterDataList = mapper.readValue(new File(
                quarterFilepath), new TypeReference<List<SelectStockData>>() {
        });

        return quarterDataList;
    }
}
