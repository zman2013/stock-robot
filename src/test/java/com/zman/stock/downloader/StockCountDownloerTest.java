package com.zman.stock.downloader;

import com.zman.stock.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Deprecated
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class StockCountDownloerTest {

    @Autowired
    private StockCountDownloader downloader;

    @Test
    public void process() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = StockCountDownloader.class.getDeclaredMethod("process",String.class);
        method.setAccessible(true);
        Map<String,String> map = (Map<String, String>) method.invoke(downloader, downloader.getBaseUrl().replace("%s","sz000848"));
        System.out.println(map.get("count"));
    }

    @Test
    public void download() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        downloader.download();

    }
}
