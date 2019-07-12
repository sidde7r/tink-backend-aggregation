package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private String scaOAuth;
    private String scaStatus;
    private String self;
    private String status;
}
