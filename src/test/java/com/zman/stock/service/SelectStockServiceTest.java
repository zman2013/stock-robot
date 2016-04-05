package com.zman.stock.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zman.stock.Application;
import com.zman.stock.selector.SelectStockByAnnualFinance;
import com.zman.stock.selector.SelectStockByBothInfo;
import com.zman.stock.selector.SelectStockByQuarterFinance;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class SelectStockServiceTest {

    @Autowired
    private SelectStockByAnnualFinance annualSelector;
    @Autowired
    private SelectStockByQuarterFinance quarterSelector;
    @Autowired
    private SelectStockByBothInfo bothSelector;

    /**
     * 按季度财务数据筛选股票
     */
    @Test
    public void runQuarterSelector() {
        quarterSelector.select();
    }

    /**
     * 按年度财务数据筛选股票
     */
    @Test
    public void runAnnualSelector() {
        annualSelector.select();
    }

    /**
     * 取季度筛选和年度筛选的交集
     */
    @Test
    public void runBothSelector() {
        bothSelector.select();
    }
}
