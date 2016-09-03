package com.zman.stock.data.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class StockFinanceBO{

    private String code;
    
    private String name;
    
    private Set<String> dateList = new TreeSet<>( new Comparator<String>(){
        public int compare( String arg0, String arg1 ){
            return arg0.compareTo( arg1 );
        }
    } );

    /** {itemName:{Date:value},...}
     * "归属于母公司股东权益合计" -> { "1512" -> 1000 } ....
     */
    private Map<String, Map<String, Float>> data = new HashMap<>();
    
    
    public void putAll( Map<String, Map<String, Float>> mapping ){
        data.putAll( mapping );
    }
    
    public void add( String date ){
        dateList.add( date );
    }
    

    public Set<String> getDateList(){
        return dateList;
    }


    public void setData( Map<String, Map<String, Float>> data ){
        this.data = data;
    }

    public String getCode(){
        return code;
    }

    public void setCode( String code ){
        this.code = code;
    }

    public String getName(){
        return name;
    }

    public void setName( String name ){
        this.name = name;
    }

    public Map<String, Map<String, Float>> getData(){
        return data;
    }

}
