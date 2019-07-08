package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private String startAuthorisation;
    private String self;
    private String status;

    public String getStartAuthorisation() {
        return startAuthorisation;
    }
}
