package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapSerializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeAgreementDetails {
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String agreement;

    public AuthorizeAgreementDetails setAgreement(
            String agreement) {
        this.agreement = agreement;
        return this;
    }
}
