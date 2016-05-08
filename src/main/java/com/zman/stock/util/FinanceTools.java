package com.zman.stock.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
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
     * @param itemList
     * @param date
     * @return 不存在返回空
     */
    public static Float getLastYearFinance(Map<String,Map<String,Float>> finance, List<String> itemList, String date){
        Float result = null;
        Map<String,Float> data = getFinance(finance, itemList);
        if( data != null ){
            String lastYear = computeLastYearDate(date);
            result = data.get(lastYear);
        }
        return result;
    }

    /**
     * 获得上一季度的财务数据，累计
     * @param finance
     * @param itemList
     * @param date
     * @return 不存在返回空
     */
    public static Float getLastSeasonFinance(Map<String,Map<String,Float>> finance, List<String> itemList, String date){
        Float result = null;
        Map<String,Float> data = getFinance(finance, itemList);
        if( data != null ){
            String lastSeason = computeLastSeason(date);
            result = data.get(lastSeason);
        }
        return result;
    }

    /**
     * 获得单季度的财务数据，单季度
     * @param finance
     * @param itemList
     * @param date
     * @return
     */
    public static Float getOneSeasonFinance(Map<String,Map<String,Float>> finance, List<String> itemList, String date){
        Float result = null;
        Map<String,Float> data = getFinance(finance, itemList);
        if( data != null ){
            Float currentSeasonFinance= data.get(date);
            if(  currentSeasonFinance != null) {
                if (!date.endsWith("03")) {
                    Float lastSeasonFinance = getLastSeasonFinance(finance, itemList, date);
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
     * @param itemList
     * @param date
     * @return
     */
    public static Float getOneLastSeasonFinance(Map<String,Map<String,Float>> finance, List<String> itemList, String date){
        Float result = null;
        date = computeLastSeason(date); //上一季度日期
        result = getOneSeasonFinance(finance, itemList, date);
        return result;
    }

    /**
     * 计算同比增长率（百分比）
     * @param finance
     * @param itemList
     * @param date
     * @return
     */
    public static String computeYearGrowth(Map<String,Map<String,Float>> finance, List<String> itemList, String date){
        String result = null;
        Map<String,Float> data = getFinance(finance, itemList);
        if(data!=null){
            Float currentValue = data.get(date);  //当期累计数据
            Float lastYearValue = getLastYearFinance(finance, itemList, date); //去年同期数据
            if( currentValue != null && lastYearValue != null ){
                float ratio = (currentValue-lastYearValue)/lastYearValue;
                result = String.format("%.2f", ratio*100);
            }
        }
        return result;
    }

    /**
     * 计算环比增长率（百分比）
     * @param finance
     * @param itemList
     * @param date
     * @return
     */
    public static String computeSeasonGrowth(Map<String,Map<String,Float>> finance, List<String> itemList, String date){
        String result = null;
        Map<String,Float> data = getFinance(finance, itemList);
        if(data!=null){
            Float currentValue = getOneSeasonFinance(finance, itemList, date); //单季数据
            Float lastSeasonValue = getOneLastSeasonFinance(finance, itemList, date); //上一季度单季数据
            if( currentValue != null && lastSeasonValue != null ){
                float ratio = (currentValue-lastSeasonValue)/lastSeasonValue;
                result = String.format("%.2f", ratio*100);
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

    /**
     * 财务报表中，相同的条目名称在不同企业总叫法不同，因此会有多个名称。
     * 为了进行统一处理，参数出入一个列表，出现频率高的在前面。
     * 如果第一个条目名称没匹配上，就尝试匹配第二个。
     * @param itemList
     * @return
     */
    public static Map<String,Float> getFinance(Map<String,Map<String,Float>> finance, List<String> itemList) {
        for (String item : itemList) {
            Map<String,Float> data = finance.get(item);
            if( data!=null ){
                return data;
            }
        }
        return Collections.emptyMap();
    }
}
