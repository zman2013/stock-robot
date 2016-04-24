package com.zman.stock.downloader;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zman.stock.Application;
import com.zman.stock.exception.DownloadFailException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class FinanceForecastDownloaderTest {

    @Autowired
    private StockFinanceForecastDownloader downloader;

    @Test
    public void download() throws DownloadFailException, IOException {
        downloader.download();
    }
}
