package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {

    private String redirect;
    private String self;

    public String getRedirect() {
        return redirect;
    }

    public String getSelf() {
        return self;
    }
}
