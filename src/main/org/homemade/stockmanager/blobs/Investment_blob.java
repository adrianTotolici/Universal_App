package org.homemade.stockmanager.blobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Investment_blob implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String stockSymbol;
    private List<Double> investment = new ArrayList<>();

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public List<Double> getInvestment() {
        return investment;
    }

    public void addInvestment(Double investment) {
        this.investment.add(investment);
    }

}
