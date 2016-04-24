package com.zman.stock.data.domain;

/**
 * 业绩预告
 * 
 * @author zman
 *
 */
public class FinanceForecast {

    public String code;

    public String name;

    /**
     * 预告类型
     */
    public String type;

    /**
     * 预告内容
     */
    public String content;

    /**
     * 净利润变动幅度
     */
    public String raisePercentage;
}
