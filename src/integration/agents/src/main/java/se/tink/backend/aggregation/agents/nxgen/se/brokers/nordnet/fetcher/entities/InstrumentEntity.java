package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentEntity {
    @JsonProperty("price_type")
    private String priceType;

    private List<TradableEntity> tradables;

    @JsonProperty("instrument_id")
    private String id;

    @JsonProperty("instrument_type")
    private String type;

    @JsonProperty("instrument_group_type")
    private String groupType;

    private String currency;

    @JsonProperty("number_of_securities")
    private double numberOfSecurities;

    private double multiplier;

    @JsonProperty("pawn_percentage")
    private double pawnPercentage;

    private String symbol;

    @JsonProperty("isin_code")
    private String isin;

    private String sector;

    @JsonProperty("sector_group")
    private String sectorGroup;

    private String name;

    private String institute;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getGroupType() {
        return groupType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getIsin() {
        return isin;
    }

    public String getName() {
        return name;
    }

    public String getInstitute() {
        return institute;
    }
}
