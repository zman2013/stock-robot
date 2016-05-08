package com.zman.stock.downloader;

import com.zman.stock.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by zman on 2016/5/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class StockBasicFinanceFromSinaDownloaderTest {

    @Autowired
    private StockBasicFinanceFromSinaDownloader downloader;

    @Test
    public void download() throws Exception {
        downloader.download("000848");
    }

}