package com.zman.stock.downloader.finance;

public class CashFlowBO{

    private String stockCode;
    
    private String date;
    
    private Float income;
    
    private Float netIncome;
    
    private Float cashFromOperations;
    
    private Float cashInvestments;
    
    private Float freeCashflow;
    
    /**
     * 筹资活动现金流
     */
    private Float cashFinancing;
    
    
    public Float getCashFinancing(){
        return cashFinancing;
    }

    public void setCashFinancing( Float cashFinancing ){
        this.cashFinancing = cashFinancing;
    }

    public String getStockCode(){
        return stockCode;
    }

    public void setStockCode( String stockCode ){
        this.stockCode = stockCode;
    }


    public Float getIncome(){
        return income;
    }

    public void setIncome( Float income ){
        this.income = income;
    }

    public String getDate(){
        return date;
    }

    public void setDate( String date ){
        this.date = date;
    }

    public Float getCashFromOperations(){
        return cashFromOperations;
    }

    public void setCashFromOperations( Float cashFromOperations ){
        this.cashFromOperations = cashFromOperations;
    }

    public Float getCashInvestments(){
        return cashInvestments;
    }

    public void setCashInvestments( Float cashInvestments ){
        this.cashInvestments = cashInvestments;
    }

    public Float getFreeCashflow(){
        return freeCashflow;
    }

    public void setFreeCashflow( Float freeCashflow ){
        this.freeCashflow = freeCashflow;
    }

    public Float getNetIncome(){
        return netIncome;
    }

    public void setNetIncome( Float netIncome ){
        this.netIncome = netIncome;
    }
    
    
    
}
