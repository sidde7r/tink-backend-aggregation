package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class SecurityHoldingContainer {
    private CustodyHoldings holdingDetail;
    private InstrumentSummary instrumentSummary;
    private SecurityIdentifier identifier;

    public Optional<Instrument> toInstrument() {
        if (holdingDetail == null || holdingDetail.hasNoValue()) {
            return Optional.empty();
        }
        if (identifier == null && instrumentSummary == null) {
            return Optional.empty();
        }
        return Optional.of(new Instrument())
                .map(instrument -> enrich(holdingDetail, instrument))
                .map(instrument -> enrich(instrumentSummary, instrument))
                .map(instrument -> enrich(identifier, instrument));
    }

    private static Instrument enrich(InstrumentEnricher instrumentEnricher, Instrument instrument) {
        return Optional.ofNullable(instrumentEnricher)
                .map(enricher -> enricher.applyTo(instrument))
                .orElse(instrument);
    }

    @VisibleForTesting
    void setHoldingDetail(CustodyHoldings holdingDetail) {
        this.holdingDetail = holdingDetail;
    }

    @VisibleForTesting
    void setInstrumentSummary(InstrumentSummary instrumentSummary) {
        this.instrumentSummary = instrumentSummary;
    }

    @VisibleForTesting
    void setSecurityIdentifier(SecurityIdentifier identifier) {
        this.identifier = identifier;
    }

    public interface InstrumentEnricher {
        Instrument applyTo(Instrument instrument);
    }
}
