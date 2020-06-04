package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SparebankConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String keyId;
    @JsonProperty @Secret private String certificate;
    @JsonProperty @Secret private String tppId;

    public String getKeyId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "KeyId"));
        return keyId;
    }

    public String getCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificate),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate"));
        return certificate;
    }

    public String getTppId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(tppId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "TPP-ID"));
        return tppId;
    }
}
