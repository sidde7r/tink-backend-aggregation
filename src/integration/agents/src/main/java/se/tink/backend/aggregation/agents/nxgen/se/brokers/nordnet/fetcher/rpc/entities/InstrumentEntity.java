package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
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
}
