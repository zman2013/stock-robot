package com.zman.stock.downloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zman.stock.Application;
import com.zman.stock.service.StockDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource("/application.properties")
public class StockHoufuquanDailyPriceHistoryDownloaderTest {

    @Autowired
    private StockHoufuquanDailyPriceHistoryDownloader downloader;
    @Autowired
    private StockDataService stockDataService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void download() throws Exception {
        downloader.download();
    }

    @Test
    public void downloadAllStockPriceHistory(){
        // 注入count、mainBusiness，如果有
        stockDataService
                .getAllStockBasicInfo().values()
                .forEach( stock -> {
                    String content = downloader.downloadPriceHistory(stock.code);
                    try {
                        objectMapper.writeValue(new File("/tmp/stock-data/price-history/"+stock.code), content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


    }

    public static final Pattern pattern = Pattern
            .compile("_(\\d+)_(\\d+)_(\\d+):\\\\\"(.*?)\\\\\",");

    /**
     * 生成60日价格与后续5日的价格走势
     * @throws IOException
     */
    @Test
    public void produceData() throws IOException {
        File targetFile = new File("/tmp/stock-price-data-5.csv");
        BufferedWriter writer = new BufferedWriter( new FileWriter(targetFile));


        String year = "";
        String month = "";
        String day = "";

        File dir = new File("/tmp/stock-data2");
        for( File f : Objects.requireNonNull(dir.listFiles())) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(f.toURI())));

                List<PriceData> priceList = new LinkedList<>();

                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    try {
                        year = matcher.group(1);
                        month = matcher.group(2);
                        day = matcher.group(3);
                        float price = Float.parseFloat(matcher.group(4));

                        PriceData priceData = new PriceData(year + month + day, price);
                        priceList.add(priceData);
                    }catch (Exception e){
                        System.err.println( f.getName() + " " + year + month + day );
                    }
                }

                //排序 -> 倒序
                Arrays.sort(priceList.toArray());

                int size = priceList.size();
                int cycle = size / 70;
                while (cycle-- > 0) {
                    int index = cycle * 70;
                    float startPointPrice = priceList.get(index).price;
                    //60日数据
                    for (; index < cycle * 70 + 60; index++) {
                        writer.write(priceList.get(index).price / startPointPrice + ",");
                    }
                    //标签: 1涨，0不涨
                    if (priceList.get(cycle * 70 + 65).price > priceList.get(cycle * 70 + 60).price) {
                        writer.write("1");
                    } else {
                        writer.write("0");
                    }
                    //换行
                    writer.newLine();
                }

            }catch (Exception e){
                e.printStackTrace();
                System.err.println( f.getName() + " " + year + month + day );
            }
        }
    }

    /**
     * 比较60日两端的价格与后续120日的价格走势
     * @throws IOException
     */
    @Test
    public void computeData() throws IOException {

        String year = "";
        String month = "";
        String day = "";

        int correctRaise = 0, errorRaise = 0;
        int correctDown = 0, errorDown = 0;

        File dir = new File("/tmp/stock-data2");
        for( File f : Objects.requireNonNull(dir.listFiles())) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(f.toURI())));

                List<PriceData> priceList = new LinkedList<>();

                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    try {
                        year = matcher.group(1);
                        month = matcher.group(2);
                        day = matcher.group(3);
                        float price = Float.parseFloat(matcher.group(4));

                        PriceData priceData = new PriceData(year + month + day, price);
                        priceList.add(priceData);
                    }catch (Exception e){
//                        System.err.println( f.getName() + " " + year + month + day );
                    }
                }

                //排序 -> 倒序
                Arrays.sort(priceList.toArray());

                int size = priceList.size();
                int cycle = size / 180;
                while (cycle-- > 0) {
                    int index = cycle * 120;
                    if( priceList.get(index+60).price > priceList.get(index).price
                        & priceList.get(index+179).price > priceList.get(index+60).price){
                        correctRaise++;
                    }else if( priceList.get(index+60).price > priceList.get(index).price
                            & priceList.get(index+179).price < priceList.get(index+60).price){
                        errorRaise++;
                    }else if( priceList.get(index+60).price < priceList.get(index).price
                            & priceList.get(index+179).price > priceList.get(index+60).price){
                        errorDown++;
                    }else if( priceList.get(index+60).price < priceList.get(index).price
                            & priceList.get(index+179).price < priceList.get(index+60).price){
                        correctDown++;
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
                System.err.println( f.getName() + " " + year + month + day );
            }
        }

        System.out.println( "correctRaise: " + correctRaise + " " + correctRaise/1.0/(errorRaise+correctRaise) );
        System.out.println( "errorRaise: " + errorRaise + " " + errorRaise/1.0/(errorRaise+correctRaise));
        System.out.println( "correctDown: " + correctDown + " " + correctDown/1.0/(correctDown+errorDown) );
        System.out.println( "errorDown: " + errorDown + " " + errorDown/1.0/(correctDown+errorDown));
    }

    //倒序排列
    class PriceData implements Comparable<PriceData>{
        public float price;
        public String date;

        public PriceData(String date, float price) {
            this.date = date;
            this.price = price;
        }

        @Override
        public int compareTo(PriceData o) {
            return Float.compare(this.price, o.price);
        }

        @Override
        public String toString() {
            return "PriceData{" + "price=" + price + ", date='" + date + '\'' + '}';
        }
    }
}
