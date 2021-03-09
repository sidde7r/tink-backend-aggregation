package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentsResponse {
    private List<InstrumentEntity> instruments;

    public List<InstrumentEntity> getInstruments() {
        return ListUtils.emptyIfNull(instruments);
    }
}
