package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href scaOAuth;
    private Href scaStatus;
    private Href self;
    private Href status;

    public String getScaOAuth() {
        return scaOAuth.getHref();
    }

    public String getScaStatus() {
        return scaStatus.getHref();
    }
}
