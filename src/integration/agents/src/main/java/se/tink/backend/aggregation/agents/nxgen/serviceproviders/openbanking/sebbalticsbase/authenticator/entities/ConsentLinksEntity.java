package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {

    private Href status;
    private Href startAuthorization;
    private Href self;

    public Href getStatus() {
        return status;
    }

    public Href getStartAuthorization() {
        return startAuthorization;
    }

    public Href getSelf() {
        return self;
    }
}
