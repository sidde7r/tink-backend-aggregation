package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class SecurityHoldingIdentifier {
    private String currency;
    private String type;

    @JsonIgnore
    public InstrumentModule.InstrumentType getTinkType() {
        return InstrumentModule.InstrumentType.valueOf(type);
    }

    public String getType() {
        return type;
    }
}
