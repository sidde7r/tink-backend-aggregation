package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {
    private String scaStatus;
    private String scaRedirect;
    private String self;
    private String status;
}
