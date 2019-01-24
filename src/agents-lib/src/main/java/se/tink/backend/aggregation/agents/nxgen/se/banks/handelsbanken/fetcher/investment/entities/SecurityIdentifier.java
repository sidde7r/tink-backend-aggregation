package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class SecurityIdentifier implements SecurityHoldingContainer.InstrumentEnricher {
    private String currency;
    private String type;

    @Override
    public Instrument applyTo(Instrument instrument) {
        instrument.setCurrency(currency);
        instrument.setRawType(type == null ? null : type.toLowerCase());
        instrument.setType(HandelsbankenSEConstants.Fetcher.Investments.InstrumentType.asType(type));
        return instrument;
    }
}
