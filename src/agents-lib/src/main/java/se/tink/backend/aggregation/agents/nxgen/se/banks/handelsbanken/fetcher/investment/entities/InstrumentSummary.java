package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class InstrumentSummary implements SecurityHoldingContainer.InstrumentEnricher {
    private InstrumentDetail instrumentDetail;

    @Override
    public Instrument applyTo(Instrument instrument) {
        if (instrumentDetail == null) {
            return instrument;
        }
        instrument.setName(instrumentDetail.toNamn());
        String isin = instrumentDetail.toIsIn();
        String marketPlace = instrumentDetail.toLista();
        instrument.setIsin(isin);
        instrument.setMarketPlace(marketPlace);
        instrument.setUniqueIdentifier(isin + (marketPlace == null ? "" : marketPlace.trim()));
        return instrument;
    }
}
