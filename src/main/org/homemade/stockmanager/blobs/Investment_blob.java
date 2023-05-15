package org.homemade.stockmanager.blobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;

public class Investment_blob implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String stockSymbol;
    private HashMap<Double, HashMap<Double, Double>> investment = new HashMap<>();
    private double totalInvestment=0;

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public HashMap<Double, HashMap<Double, Double>> getInvestment() {
        return investment;
    }

    public void setInvestment(double investment, double exchangeRate, double sharePrice) {
        HashMap<Double, Double> hashMap = new HashMap<>();
        hashMap.put(exchangeRate, sharePrice);
        this.investment.put(investment,hashMap);
        this.totalInvestment += investment;
    }

    public double getTotalInvestment() {
        return totalInvestment;
    }
}
