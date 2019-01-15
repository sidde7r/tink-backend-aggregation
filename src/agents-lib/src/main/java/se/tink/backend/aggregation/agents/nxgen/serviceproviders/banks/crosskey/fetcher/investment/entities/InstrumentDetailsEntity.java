package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class InstrumentDetailsEntity {

    private String instrumentGroup;
    private String market;

    public String getInstrumentGroup() {
        return instrumentGroup;
    }

    public String getMarket() {
        return market;
    }

    public boolean isKnownType() {
        return CrossKeyConstants.INSTRUMENT_TYPES.containsKey(instrumentGroup.toLowerCase());
    }

    public Instrument.Type getType() {
        if (instrumentGroup == null) {
            return Instrument.Type.OTHER;
        }
        return CrossKeyConstants.INSTRUMENT_TYPES.getOrDefault(instrumentGroup.toLowerCase(),
                Instrument.Type.OTHER);
    }
}
