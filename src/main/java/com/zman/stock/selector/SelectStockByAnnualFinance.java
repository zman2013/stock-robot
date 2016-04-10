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
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.StockDataTools;

/**
 * 根据营业收入增长率和净利润增长率筛选股票。 标准：近三年年报的增长率>20
 * 
 * @author zman
 *
 */
@Service
public class SelectStockByAnnualFinance {

    private static final Logger logger = LoggerFactory
            .getLogger(SelectStockByAnnualFinance.class);

    @Value("${stock.select.by.annual.finance.file}")
    private String filepath;

    @Autowired
    private StockDataService stockDataService;

    public void select() {
        try {
            selectImpl();
        } catch (Exception e) {
            logger.error("根据年度财务信息筛选股票遇到错误", e);
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

            Map<String, Map<String, String>> finance = stockDataService
                    .getBasicFinanceData(stock.code);
            stock.reportDateList = StockDataTools.computeLast3YearReportDate();

            try {
                for (int i = 0; i < stock.reportDateList.size(); i++) {
                    SelectStockByQuarterFinance.checkRaise(finance,
                            stock.reportDateList.get(i), stock);
                }

                /** pe < 50 过滤 */
                if (finance.containsKey(stock.reportDateList.get(0))) {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.reportDateList.get(0), finance);
                } else {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.reportDateList.get(1), finance);
                }
                if (stock.pe < 50) {
                    stockDataList.add(stock);
                }

            } catch (Exception e) {
                // 不合格的股票
            }
        }

        // json格式
        stockDataService.backupAndWriteNew(filepath, stockDataList);
    }

}
