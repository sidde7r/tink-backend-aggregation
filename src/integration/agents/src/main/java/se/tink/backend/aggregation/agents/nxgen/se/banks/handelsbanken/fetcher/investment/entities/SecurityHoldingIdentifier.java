package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityHoldingIdentifier {
    private String currency;
    private String type;

    @JsonIgnore
    public Instrument.Type getTinkType() {
        return HandelsbankenSEConstants.InstrumentType.asType(type);
    }

    public String getType() {
        return type;
    }
}
