package com.zman.stock.data.domain;

public class StockBasicInfo {

    /* 代码 */
    public String code;

    /* 名称 */
    public String name;

    /* 股票总股数，单位个 */
    public long count;

    /* 主营业务 */
    public String mainBusiness;

    /** 价格 */
    public String price;

    @Override
    public String toString() {
        return "StockBasicInfo [code=" + code + ", name=" + name + ", count="
                + count + ", mainBusiness=" + mainBusiness + ", price=" + price
                + "]";
    }

}
