package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyEntity {
    private String sector;
    private List<StockEntity> stocks;
    private String description;
    private String name;
    private String id;
    private long marketCapital;
    private String marketCapitalCurrency;
    private String chairman;
    private long totalNumberOfShares;

    @JsonProperty("CEO")
    private String ceo;

    public String getSector() {
        return sector;
    }

    public List<StockEntity> getStocks() {
        return stocks;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public long getMarketCapital() {
        return marketCapital;
    }

    public String getMarketCapitalCurrency() {
        return marketCapitalCurrency;
    }

    public String getChairman() {
        return chairman;
    }

    public long getTotalNumberOfShares() {
        return totalNumberOfShares;
    }

    public String getCeo() {
        return ceo;
    }
}
