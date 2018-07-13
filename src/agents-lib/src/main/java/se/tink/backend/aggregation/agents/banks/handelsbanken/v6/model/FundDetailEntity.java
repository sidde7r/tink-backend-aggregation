package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.AmountParser;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundDetailEntity {

    private String externalFundId;
    private String isin;
    private String name;
    private String navAmountText;

    public String getIsin() {
        return isin;
    }

    public String getName() {
        return name;
    }

    public String getExternalFundId() {
        return externalFundId;
    }

    public Optional<Double> parseNavAmount() {
        return new AmountParser(navAmountText).parse();
    }
}
