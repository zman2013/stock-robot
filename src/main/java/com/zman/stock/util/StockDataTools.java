package com.zman.stock.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

public class StockDataTools {

    /**
     * 计算指定报告期的pe
     * 
     * @param price
     * @param count
     * @param item
     * @param finance
     * @return
     */
    public static float computePE(double price, long count, String item,
            Map<String, Map<String, String>> finance) {

        double profit = findLastYearNetProfit(item, finance);

        Double pe = price / (profit / (count * 10000));
        return Float.parseFloat(String.format("%.2f", pe));
    }

    /**
     * 获得过去四个季度的利润
     * 
     * @param item
     * @param financeMap
     * @return
     */
    public static double findLastYearNetProfit(String item,
            Map<String, Map<String, String>> financeMap) {
        double profit = 0d;

        if (item.endsWith("年年报")) {
            profit += Double.parseDouble(financeMap.get(item).get("净利润"));
        } else if (item.endsWith("年三季报")) {
            int year = Integer.parseInt(item.replace("年三季报", ""));
            profit += Double.parseDouble(financeMap.get(year + "年三季报").get(
                    "净利润"));
            profit += Double.parseDouble(financeMap.get((year - 1) + "年年报")
                    .get("净利润"));
            profit -= Double.parseDouble(financeMap.get((year - 1) + "年三季报")
                    .get("净利润"));
        } else if (item.endsWith("年中报")) {
            int year = Integer.parseInt(item.replace("年中报", ""));
            profit += Double.parseDouble(financeMap.get(year + "年中报")
                    .get("净利润"));
            profit += Double.parseDouble(financeMap.get((year - 1) + "年年报")
                    .get("净利润"));
            profit -= Double.parseDouble(financeMap.get((year - 1) + "年中报")
                    .get("净利润"));
        } else if (item.endsWith("年一季报")) {
            int year = Integer.parseInt(item.replace("年一季报", ""));
            profit += Double.parseDouble(financeMap.get(year + "年一季报").get(
                    "净利润"));
            profit += Double.parseDouble(financeMap.get((year - 1) + "年年报")
                    .get("净利润"));
            profit -= Double.parseDouble(financeMap.get((year - 1) + "年一季报")
                    .get("净利润"));
        }

        return profit;
    }

    /**
     * 获得指定日期之前四个季度的净利润
     * 
     * @param date
     * @param financeMap
     * @return
     */
    public static double findProfit(String date,
            Map<String, Map<String, String>> financeMap) {

        String item = ""; // 报告期

        int year = Integer.parseInt(date.substring(0, 4));
        int day = Integer.parseInt(date.substring(4));

        double profit = 0d;
        if (day < 331) {
            item = (year - 1) + "年年报";
            if (!financeMap.containsKey(item)) {
                item = (year - 1) + "年三季报";
            }
        } else if (day < 630) {
            item = year + "年一季报";
        } else if (day < 930) {
            item = year + "年中报";
        } else {
            item = year + "年三季报";
        }

        profit = StockDataTools.findLastYearNetProfit(item, financeMap);

        return profit;
    }

    /**
     * 根据当前时间计算之前五个季度的报告期名称， 例如：当前时间为为2016-04-04， 则之前五个季度报告期应为：
     * "2016年一季报"，"2015年年报", "2015年三季报", "2015年中报", "2015年一季报"
     * 
     * @return
     */
    public static List<String> computeLast5QuaterItem() {
        DateTime datetime = new DateTime();
        int year = datetime.getYear();
        int month = datetime.getMonthOfYear();
        if (month > 10) {
            return Arrays.asList(year + "年三季报", year + "年中报", year + "年一季报",
                    (year - 1) + "年年报", (year - 1) + "年三季报");
        } else if (month > 7) {
            return Arrays.asList(year + "年中报", year + "年一季报", (year - 1)
                    + "年年报", (year - 1) + "年三季报", (year - 1) + "年中报");
        } else if (month > 4) {
            return Arrays.asList(year + "年一季报", (year - 1) + "年年报", (year - 1)
                    + "年三季报", (year - 1) + "年中报", (year - 1) + "年一季报");
        } else {
            return Arrays
                    .asList((year - 1) + "年年报", (year - 1) + "年三季报", (year - 1)
                            + "年中报", (year - 1) + "年一季报", (year - 2) + "年年报");
        }
    }

    /**
     * 根据当前时间计算之前三年年报的报告期名称， 
     * 例如：当前时间为为2016-04-04， 则之前3年年报报告期应为：
     * "2015年年报", "2014年年报", "2013年年报"
     * 
     * @return
     */
    public static List<String> computeLast3YearItem() {
        DateTime datetime = new DateTime();
        int year = datetime.getYear();
        return Arrays.asList((year-1) + "年年报", (year - 2) + "年年报", (year - 3)
                + "年年报");
    }
}
