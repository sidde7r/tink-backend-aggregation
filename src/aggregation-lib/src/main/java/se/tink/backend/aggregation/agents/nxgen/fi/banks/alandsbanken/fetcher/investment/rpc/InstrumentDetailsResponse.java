package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentDetailsResponse extends AlandsBankenResponse {

    private InstrumentDetailsEntity price;

    public InstrumentDetailsEntity getInstrumentDetails() {
        return price;
    }
}
