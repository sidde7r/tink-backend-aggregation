package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class Links {

    @JsonProperty("scaRedirect")
    private Href scaRedirect;
}
