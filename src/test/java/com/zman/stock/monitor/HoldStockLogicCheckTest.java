package com.zman.stock.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zman.stock.Application;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.downloader.StockHoufuquanDailyPriceHistoryDownloader;
import com.zman.stock.selector.SelectStockData;
import com.zman.stock.service.LoadSelectStockService;
import com.zman.stock.service.StockDataService;

/**
 * 根据历史数据检验买入、卖出点位是否合理
 * 
 * @author zman
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class HoldStockLogicCheckTest {

    private static final Logger logger = LoggerFactory
            .getLogger(HoldStockLogicCheckTest.class);

    @Autowired
    private StockHoufuquanDailyPriceHistoryDownloader priceDownloader;
    @Autowired
    private StockDataService stockDataService;
    @Autowired
    private LoadSelectStockService selectService;

    @Test
    public void test() throws Exception {
        Map<String, StockBasicInfo> allStockMap = stockDataService
                .getAllStockBasicInfo();
        List<SelectStockData> quarterList = selectService.loadQuarter();
        Map<String, StockBasicInfo> stockMap = new HashMap<>();
        for (SelectStockData stock : quarterList) {
            stockMap.put(stock.code, allStockMap.get(stock.code));
        }

        List<StockBasicInfo> list = new ArrayList<>(stockMap.values());

        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            StockBasicInfo stock = list.get(random.nextInt(list.size()));

            List<Price> priceList = download(stock);
            Collections.reverse(priceList);
            
            System.out.println(stock.toString());
            for (int j = 0; j < 10; j++) {
                randomCompute(priceList);
            }

        }
    }

    /**
     * 随机持有一段时间股票，持有期间按特定规律买入卖出，最后计算收益
     * 
     * @param priceList
     */
    private void randomCompute(List<Price> priceList) {
        Random random = new Random();
        int startdateIndex = random.nextInt(priceList.size());
        int stopdateIndex = random.nextInt(priceList.size());
        if (startdateIndex > stopdateIndex) {
            int tmp = startdateIndex;
            startdateIndex = stopdateIndex;
            stopdateIndex = tmp;
        }
        double totalMoney = 100000;
        double holdPrice = priceList.get(startdateIndex).price;
        double maxPrice = holdPrice;
        String maxPriceDate = priceList.get(startdateIndex).date;
        double minPrice = holdPrice;
        String minPriceDate = priceList.get(startdateIndex).date;
        double currentPrice = holdPrice;
        logger.debug("起始资金[{}],日期[{}],买入价[{}]", totalMoney,
                priceList.get(startdateIndex).date, holdPrice);
        boolean holding = true;
        for (int i = startdateIndex + 1; i < stopdateIndex; i++) {
            double p = priceList.get(i).price;
            currentPrice = p;
            if (p > maxPrice) {
                maxPriceDate = priceList.get(i).date;
                maxPrice = p;
            }
            if (p < minPrice) {
                minPriceDate = priceList.get(i).date;
                minPrice = p;
            }
            // 如果持有中
            if (holding) {
                double change = (currentPrice - maxPrice) / maxPrice;
                if (change < -0.09) {
                    totalMoney = currentPrice * (totalMoney / holdPrice);
                    minPrice = currentPrice;
                    minPriceDate = priceList.get(i).date;
                    holding = false;
                    logger.debug("卖出：日期[{}],价格[{}],剩余资金[{}]。最高价[{}],日期[{}]",
                            priceList.get(i).date, currentPrice, totalMoney,
                            maxPrice, maxPriceDate);
                }
            } else { // 如果没有持有
                double change = (currentPrice - minPrice) / minPrice;
                if (change > 0.1) {
                    holdPrice = currentPrice;
                    maxPrice = currentPrice;
                    maxPriceDate = priceList.get(i).date;
                    holding = true;
                    logger.debug("买入：日期[{}],价格[{}],剩余资金[{}]。最低价[{}],日期[{}]",
                            priceList.get(i).date, currentPrice, totalMoney,
                            minPrice, minPriceDate);
                }
            }
        }
        totalMoney = currentPrice * (totalMoney / holdPrice);
        System.out.println("开始日期:" + priceList.get(startdateIndex).date
                + ",结束日期:" + priceList.get(stopdateIndex).date + "。最后资产:"
                + totalMoney);
    }

    /**
     * 买入、卖出股票
     * 
     * @param stock
     * @return 2012-04-01之后的股价数据
     */
    private List<Price> download(StockBasicInfo stock) {
        String content = priceDownloader.downloadPriceHistory(stock.code);

        Matcher matcher = StockHoufuquanDailyPriceHistoryDownloader.pattern
                .matcher(content);

        List<Price> priceList = new LinkedList<>();
        while (matcher.find()) {
            try {
            String date = matcher.group(1).replaceAll("_", "-");
            double price = Double.parseDouble(matcher.group(2));
            if (date.compareTo("2012-04-01") >= 0) {
                priceList.add(new Price(date, price));
            }
            } catch (Exception e) {
            }
        }

        return priceList;
    }

    class Price {
        public Price(String date, double price) {
            this.date = date;
            this.price = price;
        }
        String date;
        double price;
    }
}
