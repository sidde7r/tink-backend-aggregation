package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private AuthenticatorApiEntity api;
    private boolean disabled;
    private String featureId;
    private String label;
    private String web;
}
