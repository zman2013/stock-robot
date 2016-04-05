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
import com.zman.stock.downloader.StockMainBusinessDownloader;

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
    @Autowired
    private StockMainBusinessDownloader mainBusinessDownloader;

    /**
     * 加载股票基本信息
     */
    @Test
    public void loadStockBasicInfo() {
        Collection<StockBasicInfo> list = stockDataService
                .getAllStockBasicInfo().values();
        System.out.println(list.size());
    }

    /**
     * 下载股票基本信息
     * 
     * @throws IOException
     */
    @Test
    public void basicInfoDownloader() throws IOException {
        basicInfoDownloader.download();
    }

    /**
     * 下载股票股数，并更新基本信息
     */
    @Test
    public void stockCountDownload() {
        stockCountDownloader.download();
    }

    /**
     * 下载公司主营业务，并更新基本信息
     */
    @Test
    public void stockMainBusinessDownload() {
        mainBusinessDownloader.download();
    }
}
