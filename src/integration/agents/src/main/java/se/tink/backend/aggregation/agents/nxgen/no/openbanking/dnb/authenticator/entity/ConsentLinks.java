package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentLinks {
    private Href scaRedirect;
}
