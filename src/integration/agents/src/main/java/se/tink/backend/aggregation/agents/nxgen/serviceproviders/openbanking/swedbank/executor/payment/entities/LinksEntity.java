package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkEntity scaStatus;
    private LinkEntity scaRedirect;
    private LinkEntity self;
    private LinkEntity status;

    public LinkEntity getScaStatus() {
        return scaStatus;
    }

    public LinkEntity getScaRedirect() {
        return scaRedirect;
    }
}
