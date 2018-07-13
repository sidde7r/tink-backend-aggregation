package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    @JsonProperty("@id")
    private String id;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String agreementNumber;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String agreementNickName;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String swippAccess;

    public String getId() {
        return id;
    }
    public String getAgreementNumber() {
        return agreementNumber;
    }
    public String getAgreementNickName() {
        return agreementNickName;
    }
    public String getSwipAccess() {
        return swippAccess;
    }
}
