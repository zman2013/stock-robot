package com.zman.stock.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.zman.stock.exception.DownloadFailException;

public class DownloadUtil {

    /**
     * 下载jsoup格式文档
     * 
     * @param url
     * @return
     * @throws DownloadFailException
     * @throws Exception
     */
    public static Document downloadDoc(String url) throws DownloadFailException {
        // 如果失败，重试三次
        int i = 0;
        while (i < 3) {
            try {
                Document doc = Jsoup.connect(url).get();
                return doc;
            } catch (Exception e) {
                i++;
                if (i >= 3) {
                    throw new DownloadFailException(url, e);
                }
            }
        }

        throw new DownloadFailException("此行代码不应该被执行", null);
    }
}
