package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class JOSEHeaderEntity {
    private String alg = "A256KW";
    private String kid;
    private String enc = "A128CBC-HS256";

    @JsonProperty("field_level_enc_key_version")
    private String fieldLevelEncKeyVersion;

    public JOSEHeaderEntity(String kid, String fieldLevelEncKeyVersion) {
        this.kid = kid;
        this.fieldLevelEncKeyVersion = fieldLevelEncKeyVersion;
    }

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }
}
