package com.zman.stock.service;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zman.stock.Application;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.downloader.StockBasicInfoDownloader;
import com.zman.stock.downloader.StockCountDownloader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class StockDataServiceTest {

    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private StockBasicInfoDownloader basicInfoDownloader;

    @Autowired
    private StockCountDownloader stockCountDownloader;

    @Test
    public void loadStockBasicInfo() {
        Collection<StockBasicInfo> list = stockDataService
                .getAllStockBasicInfo().values();
        System.out.println(list.size());
    }

    @Test
    public void stockCountDownload() {
        stockCountDownloader.download();
    }

    @Test
    public void basicInfoDownloader() throws IOException {
        basicInfoDownloader.download();
    }
}
