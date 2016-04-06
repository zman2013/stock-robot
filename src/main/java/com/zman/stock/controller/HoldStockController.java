package com.zman.stock.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.data.domain.HoldStockInfo;
import com.zman.stock.data.domain.StockBasicInfo;
import com.zman.stock.service.StockDataService;

@Controller
@RequestMapping("/hold")
public class HoldStockController {

    @Value("${stock.hold.info.file}")
    private String holdStockInfoFile;
    @Autowired
    private StockDataService stockDataService;

    protected final static ObjectMapper mapper = new ObjectMapper();

    @RequestMapping("list")
    public String list(Model model) throws JsonParseException,
            JsonMappingException, IOException {
        Map<String, HoldStockInfo> map = stockDataService.loadHoldStockInfo();
        model.addAttribute("stockMap", map);
        return "hold/list";
    }

    @RequestMapping("new")
    public String newStock(String code, String date) throws JsonParseException,
            JsonMappingException, IOException {
        StockBasicInfo stockBasicInfo = stockDataService.getAllStockBasicInfo()
                .get(code);
        HoldStockInfo stock = new HoldStockInfo(stockBasicInfo.name, code, date);

        File file = new File(holdStockInfoFile);
        Map<String, HoldStockInfo> map = stockDataService.loadHoldStockInfo();
        map.put(code, stock);
        mapper.writeValue(file, map);
        return "redirect:/hold/list";
    }

}