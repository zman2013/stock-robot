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
     * 根据趋势，应当执行的操作
     */
    public String action;
    /** 当前股价*/
    public String price;

    /**买入价*/
    public String buyPrice;
    /**卖出价*/
    public String sellPrice;

    /** 根据估值，应当执行的操作*/
    public String peAction;

    public HoldStockInfo() {
    }

    public HoldStockInfo(String name, String code, String date,String price) {
        this.code = code;
        this.name = name;
        this.date = date;
        this.profit = "0";
        this.action = "持有";
        this.price = price;
        this.buyPrice = "0";
        this.sellPrice = "0";
        this.peAction = "持有";
    }
}
