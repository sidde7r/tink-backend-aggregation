package se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    private Href startAuthorisationWithPsuAuthentication;
    private Href scaOAuth;
    private Href self;
    private Href status;
}
