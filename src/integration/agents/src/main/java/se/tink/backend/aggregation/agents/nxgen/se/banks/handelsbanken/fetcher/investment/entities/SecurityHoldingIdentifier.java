package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.annotations.VisibleForTesting;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Investments;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;

@JsonObject
public class SecurityHoldingIdentifier {

    private String currency;
    private String type;

    @JsonIgnore
    public InstrumentModule.InstrumentType getTinkType() {
        return Investments.INSTRUMENT_TYPE_MAPPER
                .translate(type.toLowerCase())
                .orElse(InstrumentType.OTHER);
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    @VisibleForTesting
    void setCurrency(String currency) {
        this.currency = currency;
    }

    @VisibleForTesting
    void setType(String type) {
        this.type = type;
    }
}
