package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentEntity {
    private String paperName;
    private double amount;
    private String amountTxt;
    private String id;
    private double rate;
    private String rateTxt;
    private double noOfPapers;
    private String noOfPapersTxt;
    private String currency;
    private String dataType;
    private String urlDetail;
    private long paperHash;

    public String getPaperName() {
        return paperName;
    }

    public double getAmount() {
        return amount;
    }

    public String getAmountTxt() {
        return amountTxt;
    }

    public String getId() {
        return id;
    }

    public double getRate() {
        return rate;
    }

    public String getRateTxt() {
        return rateTxt;
    }

    public double getNoOfPapers() {
        return noOfPapers;
    }

    public String getNoOfPapersTxt() {
        return noOfPapersTxt;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDataType() {
        return dataType;
    }

    public String getUrlDetail() {
        return urlDetail;
    }

    public long getPaperHash() {
        return paperHash;
    }
}
