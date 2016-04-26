package com.zman.stock.selector;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.exception.InvalidFinanceException;
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

        Set<SelectStockData> stockDataList = StockDataTools
                .createSortedSetForStockData();

        for (StockBasicInfo s : allStock) {

            SelectStockData stock = new SelectStockData();
            stock.code = s.code;
            stock.price = Double.parseDouble(s.price);
            stock.name = s.name;
            stock.mainBusiness = s.mainBusiness;
            stock.count = s.count;

            try {
                Map<String, Map<String, String>> finance = stockDataService
                        .getBasicFinanceData(stock.code);
                stock.reportDateList = StockDataTools
                        .computeLast5QuaterReportDate();

                for (int i = 0; i < stock.reportDateList.size(); i++) {
                    checkRaise(finance, stock.reportDateList.get(i), stock);
                }

                if (finance.containsKey(stock.reportDateList.get(0))) {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.reportDateList.get(0), finance);
                } else if (finance.containsKey(stock.reportDateList.get(1))) {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.reportDateList.get(1), finance);
                } else {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.reportDateList.get(2), finance);
                }
                /** pe < 50 过滤 */
                if (stock.pe < 50) {
                    stockDataList.add(stock);
                }
            } catch (InvalidFinanceException e) {
                // 不合格的股票
                // e.printStackTrace();
            } catch (Exception e) {
                // logger.error("stock[" + stock.code + "] 筛选时遇到问题", e);
            }
        }

        // json格式
        stockDataService.backupAndWriteNew(filepath, stockDataList);
    }

    /**
     * 检查利润和收入增幅是否>20% 如果报告期数据不存在，就不进行检测，直接设置为10000
     * 
     * @param finance
     * @param item
     * @param stock
     * @throws InvalidFinanceException
     */
    public static void checkRaise(Map<String, Map<String, String>> finance,
            String item, SelectStockData stock) throws InvalidFinanceException {
        checkRaise(finance, item, stock, true);
    }

    /**
     * 检查利润和收入增幅是否>20% 如果报告期数据不存在，就不进行检测，直接设置为10000
     * 
     * @param finance
     * @param item
     * @param stock
     * @param isCheck
     *            标志是否需要检查增幅
     * @throws InvalidFinanceException
     */
    public static void checkRaise(Map<String, Map<String, String>> finance,
            String item, SelectStockData stock, boolean isCheck)
            throws InvalidFinanceException {
        if (finance.containsKey(item)) {
            // 检查利润增幅
            float profitRaise = Float.parseFloat(finance.get(item).get(
                    "净利润同比增长率"));

            if (isCheck && profitRaise < 20f) {
                throw new InvalidFinanceException("profitRaise<20%,"
                        + stock.code
                        + ":" + item + ":" + profitRaise);
            }
            stock.profitRaise.add(profitRaise);
            // 检查收入增幅
            float revenueRaise = Float.parseFloat(finance.get(item).get(
                    "收入同比增长率"));

            if (isCheck && revenueRaise < 20f) {
                throw new InvalidFinanceException("revenueRaise<20%,"
                        + stock.code
                        + ":" + item + ":" + revenueRaise);
            }
            stock.revenueRaise.add(revenueRaise);
        } else {
            stock.profitRaise.add(10000f);
            stock.revenueRaise.add(10000f);
        }

    }
}
