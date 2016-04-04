package com.zman.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.zman.stock.downloader.StockBasicInfoDownloader;
import com.zman.stock.downloader.StockCountDownloader;
import com.zman.stock.downloader.StockFinanceDownloader;
import com.zman.stock.downloader.StockMainBusinessDownloader;

@SpringBootApplication
@EnableScheduling
public class Application implements CommandLineRunner {

    @Autowired
    private StockBasicInfoDownloader stockBasicInfoDownloader;
    @Autowired
    private StockCountDownloader stockCountDownloader;
    @Autowired
    private StockMainBusinessDownloader mainBusinessDownloader;
    @Autowired
    private StockFinanceDownloader stockFinanceDownloader;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... arg0) throws Exception {
        stockFinanceDownloader.download();
    }

}