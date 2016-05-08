package com.zman.stock.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 财务信息工具集，处理来自新浪的财务报表数据
 * Created by zman on 2016/5/8.
 */
public class FinanceTools {

    /** 日期格式 */
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyMM");

    /**
     * 计算上一年同期的日期，例: 201612 -> 201512
     * @param date
     * @return
     */
    public static String computeLastYearDate(String date){
        DateTime datetime = dateTimeFormatter.parseDateTime(date);
        datetime = datetime.minusYears(1);
        String text = datetime.toString(dateTimeFormatter);
        return text;
    }

    /**
     * 计算上一季度的日期，例：201612 -> 201609
     * @param date
     * @return
     */
    public static String computeLastSeason(String date){
        DateTime datetime = dateTimeFormatter.parseDateTime(date);
        datetime = datetime.minusMonths(3);
        String text = datetime.toString(dateTimeFormatter);
        return text;
    }

    /**
     * 获得上一年同期的财务数据
     * @param finance
     * @param item
     * @param date
     * @return 不存在返回空
     */
    public static Float getLastYearFinance(Map<String,Map<String,Float>> finance, String item, String date){
        Float result = null;
        Map<String,Float> data = finance.get(item);
        if( data != null ){
            String lastYear = computeLastYearDate(date);
            result = data.get(lastYear);
        }
        return result;
    }

    /**
     * 获得上一季度的财务数据，累计
     * @param finance
     * @param item
     * @param date
     * @return 不存在返回空
     */
    public static Float getLastSeasonFinance(Map<String,Map<String,Float>> finance, String item, String date){
        Float result = null;
        Map<String,Float> data = finance.get(item);
        if( data != null ){
            String lastSeason = computeLastSeason(date);
            result = data.get(lastSeason);
        }
        return result;
    }

    /**
     * 获得单季度的财务数据，单季度
     * @param finance
     * @param item
     * @param date
     * @return
     */
    public static Float getOneSeasonFinance(Map<String,Map<String,Float>> finance, String item, String date){
        Float result = null;
        Map<String,Float> data = finance.get(item);
        if( data != null ){
            Float currentSeasonFinance= data.get(date);
            if(  currentSeasonFinance != null) {
                if (!date.endsWith("03")) {
                    Float lastSeasonFinance = getLastSeasonFinance(finance, item, date);
                    if (lastSeasonFinance != null) {
                        result = currentSeasonFinance - lastSeasonFinance;
                    }
                } else {
                    result = currentSeasonFinance;
                }
            }
        }

        return result;
    }

    /**
     * 获得单上季度季度的财务数据，单季度
     * @param finance
     * @param item
     * @param date
     * @return
     */
    public static Float getOneLastSeasonFinance(Map<String,Map<String,Float>> finance, String item, String date){
        Float result = null;
        date = computeLastSeason(date); //上一季度日期
        result = getOneSeasonFinance(finance, item, date);
        return result;
    }

    /**
     * 计算同比增长率
     * @param finance
     * @param item
     * @param date
     * @return
     */
    public static String computeYearGrowth(Map<String,Map<String,Float>> finance, String item, String date){
        String result = null;
        Map<String,Float> data = finance.get(item);
        if(data!=null){
            Float currentValue = data.get(date);  //当期累计数据
            Float lastYearValue = getLastYearFinance(finance, item, date); //去年同期数据
            if( currentValue != null && lastYearValue != null ){
                float ratio = (currentValue-lastYearValue)/lastYearValue;
                result = String.format("%.2f", ratio);
            }
        }
        return result;
    }

    /**
     * 计算环比增长率
     * @param finance
     * @param item
     * @param date
     * @return
     */
    public static String computeSeasonGrowth(Map<String,Map<String,Float>> finance, String item, String date){
        String result = null;
        Map<String,Float> data = finance.get(item);
        if(data!=null){
            Float currentValue = getOneSeasonFinance(finance, item, date); //单季数据
            Float lastSeasonValue = getOneLastSeasonFinance(finance, item, date); //上一季度单季数据
            if( currentValue != null && lastSeasonValue != null ){
                float ratio = (currentValue-lastSeasonValue)/lastSeasonValue;
                result = String.format("%.2f", ratio);
            }
        }
        return result;
    }

    /**
     * 根据日期yyyyMMdd生成报告期
     * @param date
     * @return
     */
    public static String convertToVersion(String date){
        if(StringUtils.isEmpty(date)){
            throw new RuntimeException("parameter[date] is empty.");
        }
        String year = "20"+date.substring(0,2);
        if( date.endsWith("03")){
            return year+"年一季报";
        }else if(date.endsWith("06")){
            return year+"年中报";
        }else if(date.endsWith("09")){
            return year+"年三季报";
        }else if(date.endsWith("12")){
            return year+"年年报";
        }else{
            throw new RuntimeException("parameter[date="+date+"] is invalid.");
        }
    }
}
