package com.zman.stock.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zman.stock.selector.SelectStockData;
import com.zman.stock.service.LoadSelectStockService;

@Controller
public class HomeController {

    @Autowired
    private LoadSelectStockService loadSelectStockService;

    @RequestMapping("/quarter")
    public String quarter(Model model) throws Exception {
        List<SelectStockData> stockDataList = loadSelectStockService
                .loadQuarter();
        model.addAttribute("stockDataList", stockDataList);
        return "quarter";
    }
}
