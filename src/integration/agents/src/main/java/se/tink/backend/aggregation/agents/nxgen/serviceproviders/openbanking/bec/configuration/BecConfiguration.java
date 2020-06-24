package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class BecConfiguration implements ClientConfiguration {

    @JsonProperty @SensitiveSecret private String qSealc;
    @JsonProperty @SensitiveSecret private String keyId;

    public String getQsealCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(qSealc),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Qseal certificate"));
        return qSealc;
    }

    public String getKeyId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "KeyId"));
        return keyId;
    }
}
