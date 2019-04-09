package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcCustodyAsset {
    private boolean closingPrice;
    private SdcQuantity quantity;
    private SdcSecurityModel security;
    // this holds the same value as the security object, but than recalculated to DKK
    private SdcAmount valueDefaultCurrency;

    public Optional<Instrument> toInstrument() {
        if (quantity == null || quantity.getQuantity() == 0 || security == null) {
            return Optional.empty();
        }
        return Optional.of(security.toInstrument(quantity));
    }
}
