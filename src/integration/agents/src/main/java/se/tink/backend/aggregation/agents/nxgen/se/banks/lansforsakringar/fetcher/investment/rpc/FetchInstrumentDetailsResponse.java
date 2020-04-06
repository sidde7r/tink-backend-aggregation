package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchInstrumentDetailsResponse {
    private InstrumentDetailsEntity instruments;

    public InstrumentDetailsEntity getInstruments() {
        return instruments;
    }
}
