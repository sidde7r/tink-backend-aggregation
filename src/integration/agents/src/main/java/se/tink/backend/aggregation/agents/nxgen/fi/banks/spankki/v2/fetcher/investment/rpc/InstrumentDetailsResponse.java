package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities.PositionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class InstrumentDetailsResponse extends SpankkiResponse {
    @JsonProperty private PositionDetailsEntity positionDetails;

    @JsonIgnore
    public InstrumentModule toTinkInstrument(String securityId) {
        return positionDetails.toTinkInstrument(securityId);
    }
}
