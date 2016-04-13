package com.zman.stock.exception;

/**
 * 检测到财务增长率或者pe不合格的股票的异常
 * 
 * @author zman
 *
 */
public class InvalidFinanceException extends Exception {

    private static final long serialVersionUID = 6320158834720962233L;

    public InvalidFinanceException(String msg) {
        super(msg);
    }
}
