package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;

@JsonObject
public class SecurityHoldingIdentifier {

    private String currency;
    private String type;

    @JsonIgnore
    public InstrumentModule.InstrumentType getTinkType() {
        if (InstrumentType.valueOf(type) == InstrumentType.FUND
                || InstrumentType.valueOf(type) == InstrumentType.STOCK) {
            return InstrumentModule.InstrumentType.valueOf(type);
        }
        return InstrumentType.OTHER;
    }

    public String getType() {
        return type;
    }
}
