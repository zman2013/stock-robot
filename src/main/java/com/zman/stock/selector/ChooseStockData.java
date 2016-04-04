package com.zman.stock.selector;

import java.util.LinkedList;
import java.util.List;

public class ChooseStockData {

    public String code;

    public String name;

    public double price;

    public long count;

    public float pe;

    /** 主营业务 */
    public String mainBusiness;

    /** 报告期 */
    public List<String> item;
    /** 报告期内净利润涨幅 */
    public List<Float> profitRaise = new LinkedList<>();
    /** 报告期内营业收入涨幅 */
    public List<Float> revenueRaise = new LinkedList<>();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChooseStockData) {
            ChooseStockData data = (ChooseStockData) obj;
            return this.code.equals(data.code);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

}
