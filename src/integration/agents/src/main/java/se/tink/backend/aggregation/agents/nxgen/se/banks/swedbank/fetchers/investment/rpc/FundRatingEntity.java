package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FundRatingEntity {
    private String information;
    private int risk;
    private int morningstar;
    private int performance;
    private String detailedInformation;
}
