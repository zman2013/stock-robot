package com.zman.stock.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zman.stock.selector.SelectStockByAnnualFinance;
import com.zman.stock.selector.SelectStockByBothInfo;
import com.zman.stock.selector.SelectStockByQuarterFinance;
import com.zman.stock.selector.SelectStockData;
import com.zman.stock.service.LoadSelectStockService;

@Controller
@RequestMapping("/select")
public class SelectController {

    @Autowired
    private LoadSelectStockService loadSelectStockService;
    @Autowired
    private SelectStockByAnnualFinance annualSelector;
    @Autowired
    private SelectStockByQuarterFinance quarterSelector;
    @Autowired
    private SelectStockByBothInfo bothSelector;

    @RequestMapping(value = { "quarter", "", "/" })
    public String quarter(Model model) throws Exception {
        List<SelectStockData> stockDataList = loadSelectStockService
                .loadQuarter();
        model.addAttribute("stockDataList", stockDataList);
        return "select/select";
    }

    @RequestMapping("annual")
    public String annual(Model model) throws Exception {
        List<SelectStockData> stockDataList = loadSelectStockService
                .loadAnnual();
        model.addAttribute("stockDataList", stockDataList);
        return "select/select";
    }

    @RequestMapping("both")
    public String both(Model model) throws Exception {
        List<SelectStockData> stockDataList = loadSelectStockService.loadBoth();
        model.addAttribute("stockDataList", stockDataList);
        return "select/select";
    }

    @RequestMapping(value = { "run-quarter-select" })
    public String runQuarter(Model model) throws Exception {
        quarterSelector.select();
        return "redirect:/select/quarter";
    }

    @RequestMapping("run-annual-select")
    public String runQnnual(Model model) throws Exception {
        annualSelector.select();
        return "redirect:/select/annual";
    }

    @RequestMapping("run-both-select")
    public String runBoth(Model model) throws Exception {
        bothSelector.select();
        return "redirect:/select/both";
    }
}
