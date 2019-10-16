package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities;

import java.util.Collection;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

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
        Collection<Instrument.Type> knownTypes =
                CrossKeyConstants.INSTRUMENT_TYPES.getMappedTypes();
        return CrossKeyConstants.INSTRUMENT_TYPES.isOneOf(
                instrumentGroup.toLowerCase(), knownTypes);
    }

    public Instrument.Type getType() {
        if (instrumentGroup == null) {
            return Instrument.Type.OTHER;
        }
        return CrossKeyConstants.INSTRUMENT_TYPES
                .translate(instrumentGroup.toLowerCase())
                .orElse(Instrument.Type.OTHER);
    }
}
