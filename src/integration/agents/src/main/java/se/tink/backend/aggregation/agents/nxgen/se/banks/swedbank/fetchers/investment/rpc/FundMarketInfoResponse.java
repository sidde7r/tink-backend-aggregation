package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
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
}
