package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @Getter private Href startAuthorisationWithPsuAuthentication;
    private Href scaOAuth;
    private Href self;
    private Href status;
}
