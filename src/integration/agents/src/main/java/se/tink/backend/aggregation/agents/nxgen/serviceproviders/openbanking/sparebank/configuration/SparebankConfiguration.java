package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SparebankConfiguration implements ClientConfiguration {

    private String eidasCertId;
    private String redirectUrl;
    private String keyId;
    private String certificate;
    private String tppId;

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

    public String getEidasCertId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(eidasCertId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Eidas Cert ID"));
        return eidasCertId;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect Url"));
        return redirectUrl;
    }
}
