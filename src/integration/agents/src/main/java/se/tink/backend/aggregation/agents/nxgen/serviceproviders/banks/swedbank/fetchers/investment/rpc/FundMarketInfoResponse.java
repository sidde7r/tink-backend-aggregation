package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundMarketInfoResponse {
    private String name;
    private String description;
    private String category;
    private RateEntity rate;
    private String fundCode;
    private String rateDate;
    private boolean fundguide;
    private FundRatingEntity rating;
    private String tsid;
    private String startDate;
    private String fundAssets;
    private String fundAssetsUnit;
    private InfoURLsEntity infoURLs;
    private String isincode;
    private String legalResidenceCountryCode;
    private String ppmcode;
    private String performanceToday;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public RateEntity getRate() {
        return rate;
    }

    public String getFundCode() {
        return fundCode;
    }

    public String getRateDate() {
        return rateDate;
    }

    public boolean isFundguide() {
        return fundguide;
    }

    public FundRatingEntity getRating() {
        return rating;
    }

    public String getTsid() {
        return tsid;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getFundAssets() {
        return fundAssets;
    }

    public String getFundAssetsUnit() {
        return fundAssetsUnit;
    }

    public InfoURLsEntity getInfoURLs() {
        return infoURLs;
    }

    public String getIsincode() {
        return isincode;
    }

    public String getLegalResidenceCountryCode() {
        return legalResidenceCountryCode;
    }

    public String getPpmcode() {
        return ppmcode;
    }

    public String getPerformanceToday() {
        return performanceToday;
    }
}
