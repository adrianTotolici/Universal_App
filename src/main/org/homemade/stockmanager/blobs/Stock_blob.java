package org.homemade.stockmanager.blobs;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Stock_blob implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String name;
    private String sector;
    private String industry;
    private double value;
    private double divPerQ;
    private double ownShares;
    private double investment;
    private Date payDate;
    private Date annoucementDate;
    private Date exDividendDate;

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

    public double getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value.doubleValue();
    }

    public double getDivPerQ() {
        return divPerQ;
    }

    public void setDivPerQ(double divPerQ) {
        this.divPerQ = divPerQ;
    }

    public double getOwnShares() {
        return ownShares;
    }

    public void setOwnShares(double ownShares) {
        this.ownShares = ownShares;
    }

    public double getInvestment() {
        return investment;
    }

    public void setInvestment(double investment) {
        this.investment = investment;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public Date getAnnoucementDate() {
        return annoucementDate;
    }

    public void setAnnouncementDate(Date annoucementDate) {
        this.annoucementDate = annoucementDate;
    }

    public Date getExDividendDate() {
        return exDividendDate;
    }

    public void setExDividendDate(Date exDividendDate) {
        this.exDividendDate = exDividendDate;
    }
}
