package com.zman.stock.downloader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zman.stock.exception.DownloadFailException;
import com.zman.stock.util.DownloadUtil;

/**
 * 下载股票的基本财务信息
 * 
 * @author zman
 *
 */
@Service
public class StockBasicFinanceDownloader extends AbstractLoopAllStockDownloader {

    @Value("${stock.basic.finance.url}")
    private String baseUrl;

    @Value("${stock.basic.finance.dir}")
    private String baseDir;

    @Override
    protected String getFilePath(String code) {
        return baseDir + code;
    }

    @Override
    protected Map<String, ?> process(String code) throws DownloadFailException,
            IOException {
        Document doc = DownloadUtil.downloadDoc(String.format(baseUrl, code));
        Map<String, Map<String, String>> finance = new HashMap<>();
        doc.select("tbody tr").forEach(element -> {
            Map<String, String> tmp = new HashMap<>();

            Elements elements = element.select("td");
            String version = elements.get(1).text(); // 报告期

                tmp.put("发布日期", elements.get(2).text());
                tmp.put("营业收入", convertNumberUnit(elements.get(3).text()));
                tmp.put("收入同比增长率", elements.get(4).text()); // 收入同比增
                tmp.put("收入环比增长率", elements.get(5).text()); // 收入环比增
                tmp.put("净利润", convertNumberUnit(elements.get(6).text()));
                tmp.put("净利润同比增长率", elements.get(7).text()); // 净利润同比增
                tmp.put("净利润环比增长率", elements.get(8).text()); // 净利润环比增
                tmp.put("每股收益", elements.get(9).text()); // 每股收益
                tmp.put("每股净资产", elements.get(10).text()); // 每股净资产
                tmp.put("资产收益率", elements.get(11).text()); // 资产收益率
                tmp.put("每股现金流", elements.get(12).text()); // 每股现金流
                tmp.put("毛利率", elements.get(13).text()); // 毛利率

                finance.put(version, tmp);
            });
        return finance;
    }

    private static String convertNumberUnit(String number) {
        int length = number.length();
        if (number.endsWith("万")) {
            number = String.valueOf(Double.parseDouble(number.substring(0,
                    length - 1)) * 10000);
        } else if (number.endsWith("亿")) {
            number = String.valueOf(Double.parseDouble(number.substring(0,
                    length - 1)) * 10000 * 10000);
        }
        return number;
    }

}
