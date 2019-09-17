package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StockOrderEntity {

    private String paperName;
    private String id;
    private double rate;
    private double noOfPapers;
    private String noOfPapersTxt;
    private String buySellCode;
    private String tradeType;
    private String tradeStatus;
    private String supply;
    private String demand;
    private String lastTraded;
    private String urlDetail;

    public String getPaperName() {
        return paperName;
    }

    public String getId() {
        return id;
    }

    public double getRate() {
        return rate;
    }

    public double getNoOfPapers() {
        return noOfPapers;
    }

    public String getNoOfPapersTxt() {
        return noOfPapersTxt;
    }

    public String getBuySellCode() {
        return buySellCode;
    }

    public String getTradeType() {
        return tradeType;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public String getSupply() {
        return supply;
    }

    public String getDemand() {
        return demand;
    }

    public String getLastTraded() {
        return lastTraded;
    }

    public String getUrlDetail() {
        return urlDetail;
    }
}
