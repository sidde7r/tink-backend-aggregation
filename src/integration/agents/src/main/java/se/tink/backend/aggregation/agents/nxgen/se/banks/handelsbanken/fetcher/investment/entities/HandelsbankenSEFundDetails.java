package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.utils.AmountParser;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HandelsbankenSEFundDetails {

    private String isin;
    private String externalFundId;
    private String name;
    private String navAmountText;

    public String getIsin() {
        return isin;
    }

    public String getExternalFundId() {
        return externalFundId;
    }

    public String getName() {
        return name;
    }

    public Optional<Double> parseNavAmount() {
        return new AmountParser(navAmountText).parse();
    }
}
