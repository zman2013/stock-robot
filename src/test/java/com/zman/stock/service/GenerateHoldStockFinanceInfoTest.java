package com.zman.stock.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zman.stock.Application;
import com.zman.stock.selector.GenerateHoldStockFinanceInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class GenerateHoldStockFinanceInfoTest {

    @Autowired
    private GenerateHoldStockFinanceInfo generator;

    @Test
    public void generate() throws Exception {
        generator.generateFinanceInfo();
    }
}
