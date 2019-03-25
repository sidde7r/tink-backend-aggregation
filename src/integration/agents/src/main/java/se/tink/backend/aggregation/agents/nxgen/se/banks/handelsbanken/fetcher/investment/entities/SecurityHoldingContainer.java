package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class SecurityHoldingContainer {
    private CustodyHoldings holdingDetail;
    private SecurityHoldingIdentifier identifier;
    private String name;

    public Optional<Instrument> toInstrument() {
        if (holdingDetail == null || holdingDetail.hasNoValue()) {
            return Optional.empty();
        }
        if (identifier == null) {
            return Optional.empty();
        }

        Instrument tinkInstrument = new Instrument();
        tinkInstrument.setType(identifier.getTinkType());
        tinkInstrument.setRawType(identifier.getType());
        tinkInstrument.setName(name);

        return Optional.of(holdingDetail.applyTo(tinkInstrument));
    }

    @VisibleForTesting
    void setHoldingDetail(CustodyHoldings holdingDetail) {
        this.holdingDetail = holdingDetail;
    }

    @VisibleForTesting
    void setSecurityIdentifier(SecurityHoldingIdentifier identifier) {
        this.identifier = identifier;
    }
}
