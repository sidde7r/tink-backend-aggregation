package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ListSecurityDetailsResponse {
    private BigDecimal tradeTotalVolume;
    private long tradeTotalCount;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal yearHigh;
    private BigDecimal yearLow;
    private BigDecimal latestPrice;
    private String name;
    private String isin;
    private int type;
    private String securityTypeName;
    private String securitySubTypeName;
    private BigDecimal maxBidAskPrice;
    private BigDecimal latestPriceChange;
    private BigDecimal latestPriceChangePct;
    private long delayInMinutes;
    private String latestTradePriceTime;
    private String currencyCode;
    private String latestPriceDate;
    private List<VolumePriceEntity> ask;
    private List<VolumePriceEntity> bid;
    private FundDetailsEntity fundDetails;
    private List<DividendEntity> dividend;
    private StockExchangeEntity stockExchange;
    private String currentTime;
    private int responseCode;
}
