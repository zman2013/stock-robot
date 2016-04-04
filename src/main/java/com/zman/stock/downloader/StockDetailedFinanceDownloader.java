package com.zman.stock.downloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.data.domain.StockFinanceBO;
import com.zman.stock.service.StockDataService;

/**
 * 下载股票的财务数据
 * 
 * @author zman
 *
 */
@Service
public class StockDetailedFinanceDownloader {

    private static final Log logger = LogFactory
            .getLog(StockDetailedFinanceDownloader.class);

    /** 资产负债量表链接 */
    private static final String ZCFJUrl = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_BalanceSheet/displaytype/4/stockid/{stockCode}/ctrl/all.phtml";

    /** 利润量表链接 */
    private static final String LRUrl = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_ProfitStatement/displaytype/4/stockid/{stockCode}/ctrl/all.phtml";

    /** 现金流量表链接 */
    private static final String XJLUrl = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_CashFlow/displaytype/4/stockid/{stockCode}/ctrl/all.phtml";

    @Value("${stock.detailed.finance.dir}")
    private String detailedFinanceDir;

    @Autowired
    private StockDataService stockDataService;

    /**
     * 根据股票代码查询三大表，并返回数据。
     * 
     * @param stockList
     * @return
     * @throws Exception
     */
    public List<StockFinanceBO> findByStockList(List<String> stockList,
            String financeType) throws Exception {
        // 参数为空
        if (stockList.isEmpty()) {
            Collections.emptyList();
        }
        // 注入股票名称
        List<StockFinanceBO> stockFinanceBOList = retrieveStockName(stockList);
        // 注入三大报表信息
        for (StockFinanceBO stockFinanceBO : stockFinanceBOList) {
            String stockCode = stockFinanceBO.getCode();
            switch (financeType) {
            case "BalanceSheet": // 注入资产负债表数据
                Map<String, Map<String, Float>> zcfjMapping = injectFinanceData(
                        stockCode, ZCFJUrl, stockFinanceBO);
                stockFinanceBO.putAll(zcfjMapping);
                break;
            case "ProfitStatement": //
                // 注入利润表数据
                Map<String, Map<String, Float>> lrMapping = injectFinanceData(
                        stockCode, LRUrl, stockFinanceBO);
                stockFinanceBO.putAll(lrMapping);
                break;
            case "CashFlow": // 注入现金流表数据
                Map<String, Map<String, Float>> xjlMapping = injectFinanceData(
                        stockCode, XJLUrl, stockFinanceBO);
                stockFinanceBO.putAll(xjlMapping);
                break;
            case "MainFinanceData": //
                // 注入资产负债表数据
                Map<String, Map<String, Float>> zcfjMapping2 = injectFinanceData(
                        stockCode, ZCFJUrl, stockFinanceBO);
                stockFinanceBO.putAll(zcfjMapping2); // 注入利润表数据
                Map<String, Map<String, Float>> lrMapping2 = injectFinanceData(
                        stockCode, LRUrl, stockFinanceBO);
                stockFinanceBO.putAll(lrMapping2); // 注入现金流表数据
                Map<String, Map<String, Float>> xjlMapping2 = injectFinanceData(
                        stockCode, XJLUrl, stockFinanceBO);
                stockFinanceBO.putAll(xjlMapping2);
                break;
            default:
                logger.warn("财务报表类型错误：" + financeType);
                break;
            }

            outputFinanceDataToFile(financeType, stockFinanceBO);

        }

        return stockFinanceBOList;
    }

    private void outputFinanceDataToFile(String fileExtension,
            StockFinanceBO stockFinanceBO) {
        File outputFile = new File(detailedFinanceDir
                + stockFinanceBO.getCode() + "." + fileExtension);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(outputFile, stockFinanceBO);
        } catch (IOException e) {
            logger.error(
                    "将财务数据写到文件中遇到错误：" + stockFinanceBO.getCode() + "."
                            + fileExtension + ",outputFile:"
                            + outputFile.getAbsolutePath(), e);
        }
    }

    /**
     * 
     * @param stockCode
     * @param stockFinanceBO
     * @param zcfjurl
     * @return
     */
    private Map<String, Map<String, Float>> injectFinanceData(String stockCode,
            String urlTemplate, StockFinanceBO stockFinanceBO) {

        String response = "";
        int i = 0;
        while (i++ < 3) {

            URL dl = null;
            try {
                String url = urlTemplate.replace("{stockCode}", stockCode);
                dl = new URL(url);
            } catch (MalformedURLException e) {
                logger.error("构造url失败", e);
                return Collections.emptyMap();
            }

            try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                    InputStream is = dl.openStream();) {

                // this line give you the total length of source stream as a
                // String.
                // you may want to convert to integer and store this value to
                // calculate percentage of the progression.
                dl.openConnection().getHeaderField("Content-Length");

                // begin transfer by writing to dcount, not os.
                IOUtils.copy(is, os);

                response = os.toString("GBK");
                Map<String, Map<String, Float>> mapping = extractFinanceData(
                        response, stockFinanceBO);
                return mapping;
            } catch (Exception e) {
                logger.error("获取股票报表失败：" + urlTemplate, e);
            }
        }

        return null;
    }

    private static final DateFormat dateFormatter = new SimpleDateFormat(
            "yyyyMMdd");

    /**
     * 提取财务数据，并返回。{itemName:{Date:value},...}
     * 
     * @param stockFinanceBO
     * @param response
     * @return
     * @throws ParseException
     */
    private Map<String, Map<String, Float>> extractFinanceData(String content,
            StockFinanceBO stockFinanceBO) throws ParseException {

        if (StringUtils.isEmpty(content)) {
            return Collections.emptyMap();
        }

        String lineArray[] = content.split("\n");

        // 以5年为时间段，最多只获取近五年的数据。
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, year - 8);
        Date stopTime = calendar.getTime();
        // 提取日期
        String[] dateStringArray = lineArray[0].split("\t");
        int stopIndex = 1;
        for (int i = 1; i < dateStringArray.length; i++) {
            Date date = dateFormatter.parse(dateStringArray[i]);
            if (date.after(stopTime)) {
                stockFinanceBO.add(dateStringArray[i].substring(2, 6));
                stopIndex++;
            }
        }

        // 跳过单位：第二行
        Map<String, Map<String, Float>> mapping = new HashMap<>();
        for (int i = 2; i < lineArray.length; i++) {
            String[] itemArray = lineArray[i].split("\t");
            String itemName = itemArray[0];
            Map<String, Float> valueMapping = new HashMap<String, Float>(20);
            for (int j = 1; j < stopIndex && j < itemArray.length; j++) {
                Float itemValue = Float.parseFloat(itemArray[j]);
                valueMapping.put(dateStringArray[j].substring(2, 6), itemValue);
            }
            mapping.put(itemName, valueMapping);
        }

        return mapping;
    }

    /**
     * 根据股票代码，查询股票名称，并创建StockFinanceBO。
     * 
     * @param stockList
     * @return
     * @throws Exception
     */
    private List<StockFinanceBO> retrieveStockName(List<String> stockList)
            throws Exception {
        List<StockFinanceBO> stockFinanceBOList = new ArrayList<>(
                stockList.size());
        Map<String, String> stockMapping = constructStockMapping();
        for (String stock : stockList) {
            if (stockMapping.containsKey(stock)) {
                StockFinanceBO stockFinanceBO = new StockFinanceBO();
                stockFinanceBO.setCode(stock);
                String name = stockMapping.get(stock);
                stockFinanceBO.setName(name);
                stockFinanceBOList.add(stockFinanceBO);
            }
        }
        return stockFinanceBOList;
    }

    /**
     * 读取股票配置文件，并返回stockCode:stockName的mapping.
     * 
     * @return
     * @throws Exception
     */
    private Map<String, String> constructStockMapping() throws Exception {
        Map<String, String> mapping = new HashMap<>();
        Collection<StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo().values();
        allStock.forEach(stock -> {
            mapping.put(stock.code, stock.name);
        });
        return mapping;
    }

}
