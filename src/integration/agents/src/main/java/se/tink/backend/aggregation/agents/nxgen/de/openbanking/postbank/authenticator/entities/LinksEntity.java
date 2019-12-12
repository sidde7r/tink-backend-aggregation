package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("scaStatus")
    private Href scaStatusEntity;

    @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
    private Href startAuthorisationWithEncryptedPsuAuthenticationEntity;

    @JsonProperty("self")
    private Href selfEntity;

    @JsonProperty("aspspCertificates")
    private Href aspspCertificatesEntity;

    @JsonProperty("status")
    private Href statusEntity;

    @JsonProperty("authoriseTransaction")
    private Href authoriseTransactionEntity;

    public Href getScaStatusEntity() {
        return scaStatusEntity;
    }

    public Href getStartAuthorisationWithEncryptedPsuAuthenticationEntity() {
        return startAuthorisationWithEncryptedPsuAuthenticationEntity;
    }

    public Href getSelfEntity() {
        return selfEntity;
    }

    public Href getAspspCertificatesEntity() {
        return aspspCertificatesEntity;
    }

    public Href getStatusEntity() {
        return statusEntity;
    }

    public Href getAuthoriseTransactionEntity() {
        return authoriseTransactionEntity;
    }
}
