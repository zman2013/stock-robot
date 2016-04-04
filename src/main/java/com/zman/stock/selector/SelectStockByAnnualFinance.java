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
 * 根据营业收入增长率和净利润增长率筛选股票。 标准：近三年年报的增长率>20
 * 
 * @author zman
 *
 */
@Service
public class SelectStockByAnnualFinance {

    private static final Logger logger = LoggerFactory
            .getLogger(SelectStockByAnnualFinance.class);

    private static final ObjectMapper mapper = new ObjectMapper();

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
                .getAllStockBasicInfo();

        Set<ChooseStockData> stockDataList = new TreeSet<>(
                new Comparator<ChooseStockData>() {
                    public int compare(ChooseStockData d1, ChooseStockData d2) {
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
            ChooseStockData stock = new ChooseStockData();
            stock.code = s.code;
            stock.price = Double.parseDouble(s.price);
            stock.name = s.name;
            stock.mainBusiness = s.mainBusiness;
            stock.count = s.count;

            Map<String, Map<String, String>> finance = stockDataService
                    .getBasicFinanceData(stock.code);
            stock.item = StockDataTools.computeLast3YearItem();

            try {
                for (int i = 0; i < stock.item.size(); i++) {
                    SelectStockByQuarterFinance.checkRaise(finance,
                            stock.item.get(i), stock);
                }

                /** pe < 50 过滤 */
                if (finance.containsKey(stock.item.get(0))) {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.item.get(0), finance);
                } else {
                    stock.pe = StockDataTools.computePE(stock.price,
                            stock.count, stock.item.get(1), finance);
                }
                if (stock.pe < 50) {
                    stockDataList.add(stock);
                }

            } catch (Exception e) {
                // 不合格的股票
            }
        }

        // json格式
        mapper.writeValue(new File(filepath), stockDataList);
    }

}
