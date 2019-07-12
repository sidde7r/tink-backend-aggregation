package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private ScaOAuthEntity scaOAuth;
    private ScaStatusEntity scaStatus;
}
