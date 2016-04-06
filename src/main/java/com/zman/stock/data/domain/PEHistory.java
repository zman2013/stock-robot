package com.zman.stock.data.domain;

import java.util.LinkedList;
import java.util.List;

/**
 * pe历史数据，
 * 
 * 由于采集的是每月股票的最高价格、最低价格，
 * 
 * 因此根据所有最高价格生成一条pe历史数据，
 * 根据所有最低价格生成另一条pe历史数据
 * 
 * @author zman
 *
 */
public class PEHistory {
    
    public List<String> dateList = new LinkedList<>();

    public List<Float> maxPeList = new LinkedList<>();

    public List<Float> minPeList = new LinkedList<>();
}
