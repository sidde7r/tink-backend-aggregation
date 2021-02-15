package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GlobalConsentAccessEntity implements AccessEntity {
    private String allPsd2 = "allAccounts";
    private AdditionalInformation additionalInformation = new AdditionalInformation();
}
