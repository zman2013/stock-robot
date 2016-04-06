package com.zman.stock.data.domain;

public class HoldStockInfo {

    public String code;

    public String name;

    public String date;

    /**
     * 收益
     */
    public String profit;

    /**
     * 应当执行的操作
     */
    public String action;

    public HoldStockInfo() {
    }

    public HoldStockInfo(String name, String code, String date) {
        this.code = code;
        this.name = name;
        this.date = date;
        this.profit = "0";
        this.action = "持有";
    }
}
