package org.homemade.stockmanager.blobs;

import java.io.Serializable;

public class Stock_blob implements Serializable {

    private static final long serialVersionUID = 1L;

    private String symbol;
    private String name;

    public Stock_blob(String symbol, String name){
        this.symbol = symbol;
        this.name = name;
    }

    public Stock_blob(){}

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
