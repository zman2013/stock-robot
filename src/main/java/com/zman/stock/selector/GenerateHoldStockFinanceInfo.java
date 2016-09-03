package com.zman.stock.selector;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.StockDataTools;

/**
 * 生成持有的股票的基本财务信息
 * 
 * @author zman
 *
 */
@Service
public class GenerateHoldStockFinanceInfo {

    private static final Logger logger = LoggerFactory
            .getLogger(GenerateHoldStockFinanceInfo.class);

    @Value("${stock.hold.finance.file}")
    private String stockHoldFinanceFile;
    @Autowired
    private StockDataService stockDataService;

    public void generateFinanceInfo() throws Exception {
        Map<String, StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo();
        Set<SelectStockData> stockDataList = StockDataTools
                .createSortedSetForStockData();
        Map<String, HoldStockInfo> stockMap = stockDataService
                .loadHoldStockInfo();

        for (Entry<String, HoldStockInfo> entry : stockMap.entrySet()) {
            String code = entry.getKey();
            StockBasicInfo s = allStock.get(code);
            if( s == null ){
                logger.error("AllStockBasicInfo 中不存在股票:{}", code);
                continue;
            }

            SelectStockData stock = new SelectStockData();
            stock.code = s.code;
            stock.price = Double.parseDouble(s.price);
            stock.name = s.name;
            stock.mainBusiness = s.mainBusiness;
            stock.count = s.count;

            Map<String, Map<String, String>> finance = stockDataService
                    .getBasicFinanceData(code);
            stock.reportDateList = StockDataTools
                    .computeLast5QuaterReportDate();

            try {
                /**
                 * 不对持有的股票进行增幅校验
                 */
                for (int i = 0; i < stock.reportDateList.size(); i++) {
                    SelectStockByQuarterFinance.checkRaise(finance,
                            stock.reportDateList.get(i), stock, false);
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

                stockDataList.add(stock);
            } catch (Exception e) {
                logger.error("生成持有的股票财务信息出错,stock:" + code, e);
            }
        }

        stockDataService.backupAndWriteNew(stockHoldFinanceFile, stockDataList);
    }
}
