package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.HrefEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("scaStatus")
    private HrefEntity scaStatusEntity;

    @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
    private HrefEntity startAuthorisationWithEncryptedPsuAuthenticationEntity;

    @JsonProperty("self")
    private HrefEntity selfEntity;

    @JsonProperty("aspspCertificates")
    private HrefEntity aspspCertificatesEntity;

    @JsonProperty("status")
    private HrefEntity statusEntity;

    @JsonProperty("authoriseTransaction")
    private HrefEntity authoriseTransactionEntity;

    public HrefEntity getScaStatusEntity() {
        return scaStatusEntity;
    }

    public HrefEntity getStartAuthorisationWithEncryptedPsuAuthenticationEntity() {
        return startAuthorisationWithEncryptedPsuAuthenticationEntity;
    }

    public HrefEntity getSelfEntity() {
        return selfEntity;
    }

    public HrefEntity getAspspCertificatesEntity() {
        return aspspCertificatesEntity;
    }

    public HrefEntity getStatusEntity() {
        return statusEntity;
    }

    public HrefEntity getAuthoriseTransactionEntity() {
        return authoriseTransactionEntity;
    }
}
