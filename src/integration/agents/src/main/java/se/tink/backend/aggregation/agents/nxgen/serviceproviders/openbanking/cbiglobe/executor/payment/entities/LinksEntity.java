package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private HrefEntity self;
    private HrefEntity updatePsuAuthenticationRedirect;

    public HrefEntity getUpdatePsuAuthenticationRedirect() {
        return updatePsuAuthenticationRedirect;
    }
}
