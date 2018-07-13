package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentDetailsEntity {
    private String id;
    private String name;
    private String high;
    private String low;
    private String demand;
    private String supply;
    private String volume;
    private boolean aaopPctFindes;
    private int aaopPct;
    private String aaopPctTxt;
    private boolean formidlProvPctFindes;
    private int formidlProvPct;
    private String formidlProvPctTxt;
    private boolean risikoFindes;
    private int risiko;
    private String riskLabelling;
    private boolean isInvestmentTrust;
    private String urlDisclaimer;
    private String urlOrdreudfoerelsesPolitik;
    private String urlFacta;
    private String urlProspect;
    private String urlProdGrpFacta;
    private String market;
    private String isinCode;
    private String lastTradeRate;
    private String buyRate;
    private String sellRate;
    private String lastTradeTime;
    private String avgRateAllTrades;
    private String avgRate;
    private int avgRateNumber;
    private String avgRateNumberTxt;
    private String status;
    private String couponInterest;
    private String cirkVolume;
    private boolean isObligation;
    private String loanType;
    private String termDate;
    private String pubDate;
    private String currency;
    private String timestamp;
    private String rateChangePct;
    private boolean canBuy;
    private boolean canSell;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getDemand() {
        return demand;
    }

    public String getSupply() {
        return supply;
    }

    public String getVolume() {
        return volume;
    }

    public boolean isAaopPctFindes() {
        return aaopPctFindes;
    }

    public int getAaopPct() {
        return aaopPct;
    }

    public String getAaopPctTxt() {
        return aaopPctTxt;
    }

    public boolean isFormidlProvPctFindes() {
        return formidlProvPctFindes;
    }

    public int getFormidlProvPct() {
        return formidlProvPct;
    }

    public String getFormidlProvPctTxt() {
        return formidlProvPctTxt;
    }

    public boolean isRisikoFindes() {
        return risikoFindes;
    }

    public int getRisiko() {
        return risiko;
    }

    public String getRiskLabelling() {
        return riskLabelling;
    }

    public boolean isInvestmentTrust() {
        return isInvestmentTrust;
    }

    public String getUrlDisclaimer() {
        return urlDisclaimer;
    }

    public String getUrlOrdreudfoerelsesPolitik() {
        return urlOrdreudfoerelsesPolitik;
    }

    public String getUrlFacta() {
        return urlFacta;
    }

    public String getUrlProspect() {
        return urlProspect;
    }

    public String getUrlProdGrpFacta() {
        return urlProdGrpFacta;
    }

    public String getMarket() {
        return market;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public String getLastTradeRate() {
        return lastTradeRate;
    }

    public String getBuyRate() {
        return buyRate;
    }

    public String getSellRate() {
        return sellRate;
    }

    public String getLastTradeTime() {
        return lastTradeTime;
    }

    public String getAvgRateAllTrades() {
        return avgRateAllTrades;
    }

    public String getAvgRate() {
        return avgRate;
    }

    public int getAvgRateNumber() {
        return avgRateNumber;
    }

    public String getAvgRateNumberTxt() {
        return avgRateNumberTxt;
    }

    public String getStatus() {
        return status;
    }

    public String getCouponInterest() {
        return couponInterest;
    }

    public String getCirkVolume() {
        return cirkVolume;
    }

    public boolean isObligation() {
        return isObligation;
    }

    public String getLoanType() {
        return loanType;
    }

    public String getTermDate() {
        return termDate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getRateChangePct() {
        return rateChangePct;
    }

    public boolean isCanBuy() {
        return canBuy;
    }

    public boolean isCanSell() {
        return canSell;
    }
}
