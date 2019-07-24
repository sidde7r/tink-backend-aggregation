package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BecConfiguration implements ClientConfiguration {

    private String eidasQwac;
    private String qSealc;
    private String keyId;
    private String tppRedirectUrl;

    public String getEidasQwac() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasQwac),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Eidas Qwac"));
        return eidasQwac;
    }

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

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
            Strings.emptyToNull(tppRedirectUrl),
            String.format(ErrorMessages.INVALID_CONFIGURATION, "TPP redirect url"));
        return tppRedirectUrl;
    }
}
