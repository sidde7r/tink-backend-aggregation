package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class SecurityHoldingContainer {
    private CustodyHoldings holdingDetail;
    private SecurityHoldingIdentifier identifier;
    private String name;

    public Optional<InstrumentModule> toInstrumentModule() {
        if (holdingDetail == null || holdingDetail.hasNoValue()) {
            return Optional.empty();
        }
        if (identifier == null) {
            return Optional.empty();
        }

        return Optional.of(holdingDetail.applyTo(identifier, name));
    }

    @VisibleForTesting
    void setHoldingDetail(CustodyHoldings holdingDetail) {
        this.holdingDetail = holdingDetail;
    }

    @VisibleForTesting
    void setSecurityIdentifier(SecurityHoldingIdentifier identifier) {
        this.identifier = identifier;
    }

    @VisibleForTesting
    void setName(String name) {
        this.name = name;
    }
}
