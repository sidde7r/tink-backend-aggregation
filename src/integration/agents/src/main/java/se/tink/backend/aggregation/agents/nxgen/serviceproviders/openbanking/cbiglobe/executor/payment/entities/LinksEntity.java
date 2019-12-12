package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href self;
    private Href updatePsuAuthenticationRedirect;

    public Href getUpdatePsuAuthenticationRedirect() {
        return updatePsuAuthenticationRedirect;
    }
}
