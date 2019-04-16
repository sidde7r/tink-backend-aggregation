package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinksDetailsEntity scaRedirect;
    private LinksDetailsEntity status;
}
