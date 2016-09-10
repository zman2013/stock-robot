package com.zman.stock.downloader;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by zman on 2016/9/10.
 */
public class StockCountDownloerTest {

    @Test
    public void download() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        StockCountDownloader downloader = new StockCountDownloader();
        Method method = StockCountDownloader.class.getDeclaredMethod("process",String.class);
        method.setAccessible(true);
        Map<String,String> map = (Map<String, String>) method.invoke(downloader, "http://stockpage.10jqka.com.cn/000848/holder/#holdernum");
        System.out.println(map.get("count"));
    }
}
