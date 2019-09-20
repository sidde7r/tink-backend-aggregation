package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private HrefEntity scaOAuth;
    private HrefEntity scaStatus;
    private HrefEntity self;
    private HrefEntity status;

    public String getScaOAuth() {
        return scaOAuth.getHref();
    }
}
