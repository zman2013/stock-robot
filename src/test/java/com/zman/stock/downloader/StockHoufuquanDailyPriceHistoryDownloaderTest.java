package com.zman.stock.downloader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zman.stock.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class StockHoufuquanDailyPriceHistoryDownloaderTest {

    @Autowired
    private StockHoufuquanDailyPriceHistoryDownloader downloader;

    @Test
    public void download() throws Exception {
        downloader.download();
    }
}
