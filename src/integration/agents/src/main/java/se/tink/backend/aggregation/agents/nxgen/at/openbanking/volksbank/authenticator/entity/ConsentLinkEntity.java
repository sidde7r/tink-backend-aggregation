package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinkEntity {

    private String href;

    public String getHref() {
        return href;
    }
}
