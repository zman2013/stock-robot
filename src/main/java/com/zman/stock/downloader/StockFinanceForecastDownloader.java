package com.zman.stock.downloader;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zman.stock.data.domain.FinanceForecast;
import com.zman.stock.service.StockDataService;
import com.zman.stock.util.DownloadUtil;

/**
 * 下载业绩预告内容，并存到文件中。只下载第一页
 * 
 * @author zman
 *
 */
@Service
public class StockFinanceForecastDownloader {

    private static final Logger logger = LoggerFactory
            .getLogger(StockFinanceForecastDownloader.class);

    @Value("${finance-forecast-url}")
    private String url;

    @Autowired
    private StockDataService stockDataService;

    public void download() {
        try {
            // 抓取页面
            Document doc = DownloadUtil.downloadDoc(url);
            Map<String, FinanceForecast> map = new HashMap<>();

            // 解析页面
            doc.select("table.m-table tbody tr").forEach(tr -> {
                Elements eles = tr.select("td");
                FinanceForecast ff = new FinanceForecast();
                ff.code = eles.get(1).text();
                ff.name = eles.get(2).text();
                ff.type = eles.get(3).text();
                ff.content = eles.get(4).text();
                ff.raisePercentage = eles.get(5).text();
                map.put(ff.code, ff);
            });

            // 输出到文件
            stockDataService.writeFinanceForecast(map);
        } catch (Exception e) {
            logger.error("下载股票业绩预告出错", e);
        }

    }

}
