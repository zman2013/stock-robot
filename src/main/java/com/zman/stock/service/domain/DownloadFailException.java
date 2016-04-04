package com.zman.stock.service.domain;

/**
 * 下载页面失败
 * 
 * @author zman
 *
 */
public class DownloadFailException extends Exception {

    private static final long serialVersionUID = 5107355142510916234L;

    private static final String Title = "下载页面失败";

    public DownloadFailException(String url, Throwable e) {
        super(Title + url, e);
    }
}
