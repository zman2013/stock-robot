package com.zman.stock.data.domain;

import java.util.LinkedList;
import java.util.List;

public class CashFlowData {

    /**
     * 条目：净利润、收入、经营现金流等
     */
    public String item;

    /**
     * 值（与报告期一一对应）
     */
    public List<String> value = new LinkedList<>();

}
