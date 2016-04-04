package com.zman.stock.selector;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.StockDataTools;

/**
 * 根据营业收入增长率和净利润增长率筛选股票。 标准：三年每年的增长率>20 或 近四个季度的增长率>20
 * 
 * @author zman
 *
 */
@Service
public class SelectStockByQuarterFinance {

    private static final Logger logger = LoggerFactory
            .getLogger(SelectStockByQuarterFinance.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${stock.select.by.quarter.finance.file}")
    private String filepath;

    @Autowired
    private StockDataService stockDataService;

    public void select() {
        try {
            selectImpl();
        } catch (Exception e) {
            logger.error("按季度财报筛选股票时遇到错误", e);
        }
    }

    private void selectImpl() throws Exception {

        Collection<StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo().values();

        Set<SelectStockData> stockDataList = new TreeSet<>(
                new Comparator<SelectStockData>() {
                    public int compare(SelectStockData d1, SelectStockData d2) {
                        int result = -d1.revenueRaise.get(0).compareTo(
                                d2.revenueRaise.get(0));
                        if (result == 0) {
                            result = -d1.profitRaise.get(0).compareTo(
                                    d2.profitRaise.get(0));
                        }
                        if (result == 0) {
                            result = -d1.revenueRaise.get(1).compareTo(
                                    d2.revenueRaise.get(1));
                        }
                        if (result == 0) {
                            result = -d1.profitRaise.get(1).compareTo(
                                    d2.profitRaise.get(1));
                        }
                        return result;
                    }
                });

        for (StockBasicInfo s : allStock) {
            SelectStockData stock = new SelectStockData();
            stock.code = s.code;
            stock.price = Double.parseDouble(s.price);
            stock.name = s.name;
            stock.mainBusiness = s.mainBusiness;
            stock.count = s.count;

            Map<String, Map<String, String>> finance = stockDataService
                    .getBasicFinanceData(stock.code);
            stock.reportDateList = StockDataTools
                    .computeLast5QuaterReportDate();

            try {
                /**
                 * 当季的财务报告可能未发布，因此对第一个信息不进行检查
                 */
                for (int i = 0; i < stock.reportDateList.size(); i++) {
                    checkRaise(finance, stock.reportDateList.get(i), stock);
                }

                if (finance.containsKey(stock.reportDateList.get(0))) {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.reportDateList.get(0), finance);
                } else {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.reportDateList.get(1), finance);
                }
                /** pe < 50 过滤 */
                if (stock.pe < 50) {
                    stockDataList.add(stock);
                }
            } catch (Exception e) {
                // 不合格的股票
                // e.printStackTrace();
            }
        }

        // json格式
        mapper.writeValue(new File(filepath), stockDataList);
    }

    /**
     * 检查利润和收入增幅是否>20% 如果报告期数据不存在，就不进行检测，直接设置为10000
     * 
     * @param finance
     * @param item
     * @param stock
     */
    public static void checkRaise(Map<String, Map<String, String>> finance,
            String item, SelectStockData stock) {
        if (finance.containsKey(item)) {
            // 检查利润增幅
            float profitRaise = Float.parseFloat(finance.get(item).get(
                    "净利润同比增长率"));

            if (profitRaise < 20f) {
                throw new RuntimeException("profitRaise<20%," + stock.code
                        + ":" + item + ":" + profitRaise);
            }
            stock.profitRaise.add(profitRaise);
            // 检查收入增幅
            float revenueRaise = Float.parseFloat(finance.get(item).get(
                    "收入同比增长率"));

            if (revenueRaise < 20f) {
                throw new RuntimeException("revenueRaise<20%," + stock.code
                        + ":" + item + ":" + revenueRaise);
            }
            stock.revenueRaise.add(revenueRaise);
        } else {
            stock.profitRaise.add(Float.parseFloat(finance.get(item).get(
                    "净利润同比增长率")));
            stock.revenueRaise.add(Float.parseFloat(finance.get(item).get(
                    "收入同比增长率")));
        }

    }
}
