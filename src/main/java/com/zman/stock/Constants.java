package com.zman.stock;

public class Constants {

    // 项目的根目录，用来存放各种数据文件
    public static final String root = "C:\\Users\\zman\\360pan\\股票\\";

    public static final String allStockPath = "all-stocks.txt";

    // 包含股票的主营业
    public static final String StockInfoPath = Constants.root
            + "stock-info.txt";

    public static final String stockCurrentPricePath = Constants.root
            + "all-stock-current-price.txt";

    // 不复权周级股票价格
    public static String weekPricePath = Constants.root + "week-price\\";

    // 股票总股数（万）
    public final static String StockCountDir = Constants.root + "stock-count//";

    // 股票财务数据， 简要
    public static final String FinanceDir = Constants.root + "finance//";

    // 股票财务数据，详细，三大报表
    public static final String FinanceDetailDir = Constants.root
            + "finance-detail//";

    // 近三年财务数据，用于js显示
    public static final String Last3YearsData = Constants.root
            + "3-years-data-json.js";

    // 最终选出的股票信息
    public static final String ChooseStockPath = Constants.root
            + "choose-stock//";

    // 最终取交集后选出的股票，json格式
    public static final String ChooseFinalStockPathJson = ChooseStockPath
            + "final-choose-stock-finance.json";
    // text格式
    public static final String ChooseFinalStockPathText = ChooseStockPath
            + "final-choose-stock-finance.txt";

    // 选股后，股票的主要财务信息目录
    public static final String ChooseStockMainFinance = ChooseStockPath
            + "stock-main-finance//";

    // 复权前的股票价格目录
    public static final String FuquanqianMonthPricePath = Constants.root
            + "fuquanqian-month-price//";

    // pe历史数据，同一个文件可以包含多个股票
    // public static final String PEHistory = Constants.root + "pe.json";
    public static final String PEHistory = "C:\\cygwin64\\home\\zman\\workspace\\stock\\"
            + "stock-analysis\\src\\main\\java\\com\\zman\\stock\\pe.js";

}
