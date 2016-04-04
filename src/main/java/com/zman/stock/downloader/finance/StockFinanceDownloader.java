package com.zman.stock.downloader.finance;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.selector.ChooseStockData;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.StockDataTools;

/**
 * 下载并输出目标股票的主要财务指标： 营收、净利润、现金流
 * 
 * @author zman
 *
 */
@Service
public class StockFinanceDownloader {

    private static final Log logger = LogFactory
            .getLog(StockFinanceDownloader.class);

    /** 资产负债量表链接 */
    private static final String ZCFJUrl = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_BalanceSheet/displaytype/4/stockid/{stockCode}/ctrl/all.phtml";

    /** 利润量表链接 */
    private static final String LRUrl = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_ProfitStatement/displaytype/4/stockid/{stockCode}/ctrl/all.phtml";

    /** 现金流量表链接 */
    private static final String XJLUrl = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_CashFlow/displaytype/4/stockid/{stockCode}/ctrl/all.phtml";

    private RestTemplate restTemplate;

    @Value("${stock.detailed.finance.dir}")
    private String detailedFinanceDir;

    @Autowired
    private StockDataService stockDataService;

    private static ObjectMapper mapper = new ObjectMapper();

    public StockFinanceDownloader() {
        restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(getMessageConverters());
    }

    public void main(String[] args) throws Exception {
        StockFinanceDownloader service = new StockFinanceDownloader();
        Set<ChooseStockData> finalStockData = mapper.readValue(new File(
                Constants.ChooseFinalStockPathJson),
                new TypeReference<Set<ChooseStockData>>() {
                });
        List<String> stockList = new LinkedList<>();
        finalStockData.stream().forEach(stock -> {
            stockList.add(stock.code);
        });

        List<StockFinanceBO> financeList = service.findByStockList(stockList,
                "CashFlow");
        System.out.println(financeList.size());
    }

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
        /*
         * for (StockFinanceBO stockFinanceBO : stockFinanceBOList) { String
         * stockCode = stockFinanceBO.getCode(); switch (financeType) { case
         * "BalanceSheet": // 注入资产负债表数据 Map<String, Map<String, Float>>
         * zcfjMapping = injectFinanceData( stockCode, ZCFJUrl, stockFinanceBO);
         * stockFinanceBO.putAll(zcfjMapping); break; case "ProfitStatement": //
         * 注入利润表数据 Map<String, Map<String, Float>> lrMapping =
         * injectFinanceData( stockCode, LRUrl, stockFinanceBO);
         * stockFinanceBO.putAll(lrMapping); break; case "CashFlow": // 注入现金流表数据
         * Map<String, Map<String, Float>> xjlMapping = injectFinanceData(
         * stockCode, XJLUrl, stockFinanceBO);
         * stockFinanceBO.putAll(xjlMapping); break; case "MainFinanceData": //
         * 注入资产负债表数据 Map<String, Map<String, Float>> zcfjMapping2 =
         * injectFinanceData( stockCode, ZCFJUrl, stockFinanceBO);
         * stockFinanceBO.putAll(zcfjMapping2); // 注入利润表数据 Map<String,
         * Map<String, Float>> lrMapping2 = injectFinanceData( stockCode, LRUrl,
         * stockFinanceBO); stockFinanceBO.putAll(lrMapping2); // 注入现金流表数据
         * Map<String, Map<String, Float>> xjlMapping2 = injectFinanceData(
         * stockCode, XJLUrl, stockFinanceBO);
         * stockFinanceBO.putAll(xjlMapping2); break; default:
         * logger.warn("财务报表类型错误：" + financeType); break; }
         * 
         * outputFinanceDataToFile(financeType, stockFinanceBO);
         * 
         * outputMainFinanceDataForDecision(stockFinanceBO);
         * 
         * }
         */
        outputPEHistory(stockFinanceBOList);

        return stockFinanceBOList;
    }

    private void outputPEHistory(List<StockFinanceBO> stockFinanceBOList)
            throws Exception {
        List<LastThreeYearsData> result = new LinkedList<>();
        for (StockFinanceBO bo : stockFinanceBOList) {
            try {
                String code = bo.getCode();
                StockPE pe = StockDataTools.getPEHistory(code);
                LastThreeYearsData data = new LastThreeYearsData();
                data.stockCode = code;
                data.data.put("pe", pe.peList);
                data.labels.put("pe", pe.dateList);
                result.add(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("var items = ['pe']").append(";");
        sb.append("var json = ").append(mapper.writeValueAsString(result));

        FileUtils.write(new File(Constants.PEHistory), sb, "utf-8");
    }

    /**
     * 输出主要财务指标，用于选股
     * 
     * @param stockFinanceBO
     * @throws Exception
     */
    private void outputMainFinanceDataForDecision(StockFinanceBO stockFinanceBO)
            throws Exception {

        String stockCode = stockFinanceBO.getCode();
        Map<String, Map<String, String>> finance = StockDataTools
                .getFinanceData(stockCode);

        // 财报日期
        String[] cashDateArray = new String[] { "1512", "1509", "1506", "1503",
                "1412", "1409", "1406", "1403", "1312", "1309", "1306", "1303" };
        String[] financeDateArray = new String[] { "2015年年报", "2015年三季报",
                "2015年中报", "2015年一季报", "2014年年报", "2014年三季报", "2014年中报",
                "2014年一季报", "2013年年报", "2013年三季报", "2013年中报", "2013年一季报", };
        // 财务指标
        String[] cashItemArray = new String[] { "经营活动产生的现金流量净额",
                "投资活动产生的现金流量净额", "筹资活动产生的现金流量净额" };
        String[] financeItemArray = new String[] { "营业收入", "净利润" };

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(
                Constants.ChooseStockMainFinance + stockCode + ".html"))) {
            writer.write("<html><meta http-equiv='Content-Type' content='text/html; charset=utf-8' />"
                    + "<style type='text/css'>td{text-align:right;padding-left:30px}</style>"
                    + "<table style='text-align:right'><tr>");
            // 输出标题
            writer.write("<td>财务指标</td>");
            for (String date : cashDateArray) {
                writer.write("<td>" + date + "</td>");
            }
            writer.write("</tr>");

            // 输出营收和净利润
            for (String item : financeItemArray) { // 财务指标
                writer.write("<tr>");
                writer.write("<td>" + item + "</td>");
                for (String date : financeDateArray) { // 报告期
                    try {
                        writer.write("<td>"
                                + String.format("%.2f", Double
                                        .parseDouble(finance.get(date)
                                                .get(item)) / 100000000) // 除以一亿
                                + "</td>");
                    } catch (Exception e) {
                        writer.write("<td></td>");
                    }
                }

                writer.write("</tr>");
            }

            // 输出现金流
            for (String item : cashItemArray) { // 财务指标
                writer.write("<tr>");
                writer.write("<td>" + item + "</td>");
                for (String date : cashDateArray) { // 报告期
                    try {
                        writer.write("<td>"
                                + String.format(
                                        "%.2f\t",
                                        stockFinanceBO.getData().get(item)
                                                .get(date) / 100000000)
                                + "</td>");

                    } catch (Exception e) {
                        writer.write("<td></td>");
                    }
                }

                writer.write("</tr>");
            }
            writer.write("</table></html>");
        }

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

    private static List<HttpMessageConverter<?>> getMessageConverters() {
        List<HttpMessageConverter<?>> converterList = new ArrayList<>();
        StringHttpMessageConverter converter = new StringHttpMessageConverter();
        MediaType mediaType = new MediaType("application", "vnd.ms-excel",
                Charset.forName("UTF-8"));
        // MediaType mediaType = new MediaType( "text", "html", Charset.forName(
        // "gb2312" ) );
        List<MediaType> mediaTypeList = new ArrayList<>(1);
        mediaTypeList.add(mediaType);
        converter.setSupportedMediaTypes(mediaTypeList);
        converterList.add(converter);
        return converterList;
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
    public Map<String, String> constructStockMapping() throws Exception {
        Map<String, String> mapping = new HashMap<>();
        Collection<StockBasicInfo> allStock = stockDataService
                .getAllStockBasicInfo();
        allStock.forEach(stock -> {
            mapping.put(stock.code, stock.name);
        });
        return mapping;
    }

    /**
     * 分析股票的现金流 1. 读取股票财务信息，json文件 2. 写入文件
     * 
     * @throws IOException
     */
    public void analyzeCashFlow() {
        try {
            analyzeCashFlowImpl();
        } catch (IOException e) {
            logger.error("提取股票现金流信息时遇到错误，退出");
        }
    }

    /**
     * 分析股票的现金流 1. 读取股票财务信息，json文件 2. 写入文件
     * 
     * @throws IOException
     */
    private void analyzeCashFlowImpl() throws IOException {

        String cashflowFilePath = detailedFinanceDir + File.separator
                + "cashflow.csv";

        String financeDirPath = detailedFinanceDir + File.separator + "finance";
        File financeDir = new File(financeDirPath);
        if (!financeDir.exists()) {
            logger.error("股票的财务数据不存在 ，目录不存在：" + financeDirPath);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<CashFlowBO> cashflowBOList = new LinkedList<>();
        try (BufferedWriter cashflowWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(cashflowFilePath),
                        "utf-8"))) {
            for (File stockDir : financeDir.listFiles()) {
                cashflowBOList.clear();

                String stockCode = stockDir.getName();
                Map<String, Map<String, Float>> cashFlowMap = readFinanceDataFromJson(
                        stockDir, stockCode, ".CashFlow", objectMapper);
                Map<String, Map<String, Float>> profitMap = readFinanceDataFromJson(
                        stockDir, stockCode, ".ProfitStatement", objectMapper);

                String incomeItemName = "一、营业总收入";
                String netIncomeItemName = "四、净利润";
                String cashFromOperationsItemName = "经营活动产生的现金流量净额";
                String cashInvestmentsItemName = "投资活动产生的现金流量净额";
                String cashFinancingItemName = "筹资活动产生的现金流量净额";

                Map<String, Float> dateMapping = cashFlowMap
                        .get(cashFromOperationsItemName);
                if (dateMapping == null) {
                    logger.error("stock[" + stockCode + "]没有该项："
                            + cashFromOperationsItemName);
                    continue;
                }
                Set<String> dateSet = dateMapping.keySet();
                for (String date : dateSet) {
                    if (profitMap.get(incomeItemName) == null) {
                        incomeItemName = "一、营业收入";
                        if (profitMap.get(incomeItemName) == null) {
                            logger.error("stock[" + stockCode + "]没有该项："
                                    + incomeItemName);
                            break;
                        }
                    }
                    Float income = profitMap.get(incomeItemName).get(date);

                    if (profitMap.get(netIncomeItemName) == null) {
                        netIncomeItemName = "四、利润总额";
                        if (profitMap.get(netIncomeItemName) == null) {
                            logger.error("stock[" + stockCode + "]没有该项："
                                    + netIncomeItemName);
                            break;
                        }
                    }
                    Float netIncome = profitMap.get(netIncomeItemName)
                            .get(date);

                    if (cashFlowMap.get(cashFromOperationsItemName) == null) {
                        logger.error("stock[" + stockCode + "]没有该项："
                                + cashFromOperationsItemName);
                        break;
                    }
                    Float cashFromOperations = cashFlowMap.get(
                            cashFromOperationsItemName).get(date);

                    if (cashFlowMap.get(cashInvestmentsItemName) == null) {
                        logger.error("stock[" + stockCode + "]没有该项："
                                + cashInvestmentsItemName);
                        break;
                    }
                    Float cashInvestments = cashFlowMap.get(
                            cashInvestmentsItemName).get(date);

                    if (cashFlowMap.get(cashFinancingItemName) == null) {
                        logger.error("stock[" + stockCode + "]没有该项："
                                + cashFinancingItemName);
                        break;
                    }
                    Float cashFinancing = cashFlowMap
                            .get(cashFinancingItemName).get(date);

                    CashFlowBO cashflowBO = new CashFlowBO();
                    cashflowBO.setDate(date);
                    cashflowBO.setStockCode(stockCode);
                    cashflowBO.setIncome(income);
                    cashflowBO.setNetIncome(netIncome);
                    cashflowBO.setCashFromOperations(cashFromOperations);
                    cashflowBO.setCashInvestments(cashInvestments);
                    cashflowBO.setFreeCashflow(cashFromOperations
                            - cashInvestments);
                    cashflowBO.setCashFinancing(cashFinancing);
                    cashflowBOList.add(cashflowBO);
                }

                // 输出
                for (CashFlowBO cashFlowBO : cashflowBOList) {
                    cashflowWriter
                            .append(cashFlowBO.getStockCode())
                            .append(",")
                            .append("20" + cashFlowBO.getDate() + "30")
                            .append(",")
                            .append(String.valueOf(cashFlowBO.getIncome()))
                            .append(",")
                            .append(String.valueOf(cashFlowBO.getNetIncome()))
                            .append(",")
                            .append(String.valueOf(cashFlowBO
                                    .getCashFromOperations()))
                            .append(",")
                            .append(String.valueOf(cashFlowBO
                                    .getCashInvestments()))
                            .append(",")
                            .append(String.valueOf(cashFlowBO.getFreeCashflow()))
                            .append(",")
                            .append(String.valueOf(cashFlowBO
                                    .getCashFinancing()));
                    cashflowWriter.newLine();
                }
                cashflowWriter.flush();
            }
        }

    }

    /**
     * 从json文件中读取财务数据
     * 
     * @param stockDir
     * @param fileExtension
     * @param objectMapper
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private Map<String, Map<String, Float>> readFinanceDataFromJson(
            File stockDir, String stockCode, String fileExtension,
            ObjectMapper objectMapper) throws IOException {
        File jsonFile = new File(stockDir, stockCode + fileExtension);
        String fileContent = FileUtils.readFileToString(jsonFile);
        StockFinanceBO stockFinanceBO = objectMapper.readValue(fileContent,
                StockFinanceBO.class);
        Map<String, Map<String, Float>> mapping = stockFinanceBO.getData();

        return mapping;
    }

}
