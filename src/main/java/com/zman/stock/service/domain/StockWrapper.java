package com.zman.stock.service.domain;

import java.util.LinkedList;
import java.util.List;

import com.zman.stock.data.domain.Stock;

public class StockWrapper {
    public int pageCount;

    public List<Stock> stockList = new LinkedList<>();
}
