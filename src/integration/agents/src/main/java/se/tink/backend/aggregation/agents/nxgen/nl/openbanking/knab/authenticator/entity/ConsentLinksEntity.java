package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.entity;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {

    private Href self;
    private Href status;
    private Href scaOAuth;
}
