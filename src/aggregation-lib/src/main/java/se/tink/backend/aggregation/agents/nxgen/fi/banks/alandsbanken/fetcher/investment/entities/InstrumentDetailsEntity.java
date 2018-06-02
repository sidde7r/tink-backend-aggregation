package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;
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
        return AlandsBankenConstants.INSTRUMENT_TYPES.containsKey(instrumentGroup.toLowerCase());
    }

    public Instrument.Type getType() {
        if (instrumentGroup == null) {
            return Instrument.Type.OTHER;
        }
        return AlandsBankenConstants.INSTRUMENT_TYPES.getOrDefault(instrumentGroup.toLowerCase(),
                Instrument.Type.OTHER);
    }
}
