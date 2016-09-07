package com.zman.stock.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.selector.SelectStockData;
import com.zman.stock.service.LoadSelectStockService;
import com.zman.stock.service.StockDataService;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/hold")
public class HoldStockController {

    @Value("${stock.hold.info.file}")
    private String holdStockInfoFile;
    @Autowired
    private StockDataService stockDataService;
    @Autowired
    private LoadSelectStockService loadSelectStockService;

    protected final static ObjectMapper mapper = new ObjectMapper();

    @RequestMapping("list")
    public String list(Model model) throws Exception {
        Map<String, HoldStockInfo> map = stockDataService.loadHoldStockInfo();
        model.addAttribute("stockMap", map);
        return "hold/list";
    }

    @RequestMapping("new")
    public String newStock(String code, String date) throws Exception {
        StockBasicInfo stockBasicInfo = stockDataService.getAllStockBasicInfo()
                .get(code);
        HoldStockInfo stock = new HoldStockInfo(stockBasicInfo.name, code, date, stockBasicInfo.price);

        File file = new File(holdStockInfoFile);
        Map<String, HoldStockInfo> map = stockDataService.loadHoldStockInfo();
        map.put(code, stock);
        mapper.writeValue(file, map);
        return "redirect:/hold/list";
    }

    @RequestMapping("finance-list")
    public String finance(Model model) throws Exception {
        List<SelectStockData> stockDataList = loadSelectStockService
                .loadHoldStockData();
        model.addAttribute("stockDataList", stockDataList);
        return "select/select";
    }

    @RequestMapping("set-price")
    @ResponseBody
    public String setPrice(String code, String buyPrice, String sellPrice) throws Exception {
        Map<String, HoldStockInfo> map = stockDataService.loadHoldStockInfo();
        HoldStockInfo stockInfo = map.get(code);
        if( !StringUtils.isEmpty(buyPrice) || !StringUtils.isEmpty(sellPrice)) {
            if (!StringUtils.isEmpty(buyPrice)) {
                stockInfo.buyPrice = buyPrice;
                if( Float.parseFloat(stockInfo.price) < Float.parseFloat(stockInfo.buyPrice) ){
                    stockInfo.peAction = "买入";
                }
            }
            if (!StringUtils.isEmpty(sellPrice)) {
                stockInfo.sellPrice = sellPrice;
                if( Float.parseFloat(stockInfo.price) > Float.parseFloat(stockInfo.sellPrice) ){
                    stockInfo.peAction = "卖出";
                }
            }
            stockDataService.writeHoldStockInfo(map);
            return "success";
        }
        return "no change";
    }
}
