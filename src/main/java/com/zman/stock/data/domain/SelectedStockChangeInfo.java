package com.zman.stock.data.domain;

import java.util.LinkedList;
import java.util.List;

import com.zman.stock.selector.SelectStockData;

/**
 * 筛选出的股票变动信息：从筛选列表中消失、出现
 * 
 * @author zman
 *
 */
public class SelectedStockChangeInfo {

    public List<SelectStockData> newStockList = new LinkedList<>();

    public List<SelectStockData> removedStockList = new LinkedList<>();
}
