package com.zman.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.zman.stock.data.dao.StockMapper;
import com.zman.stock.data.domain.Stock;
import com.zman.stock.downloader.StockBasicInfoDownloader;

@SpringBootApplication
@EnableScheduling
public class Application implements CommandLineRunner {

    @Autowired
    private StockBasicInfoDownloader basicInfoDownloader;

    @Autowired
    private StockMapper stockMapper;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... arg0) throws Exception {
        Stock stock = new Stock();
        stock.setCode("000847");
        stock.setName("asdf");
        stock.setCount(100);
        stock.setMainBusiness("asdf");
        System.out.println(stockMapper.insert(stock));
    }

}