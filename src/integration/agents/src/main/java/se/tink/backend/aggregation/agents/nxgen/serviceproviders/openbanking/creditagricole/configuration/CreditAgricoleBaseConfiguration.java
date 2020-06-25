package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class CreditAgricoleBaseConfiguration implements ClientConfiguration {

    @Secret private String clientId;

    @Secret private String certificateId;

    @Secret private String clientSigningCertificateSerialNumber;

    @Secret private String psuIpAddress;

    @Secret private String authorizeUrl;

    @Secret private String baseUrl;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client id"));
        return clientId;
    }

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate id"));
        return certificateId;
    }

    public String getClientSigningCertificateSerialNumber() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificateSerialNumber),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate serial"));
        return clientSigningCertificateSerialNumber;
    }

    public String getPsuIpAddress() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIpAddress),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "psuIpAddress"));
        return psuIpAddress;
    }

    public String getAuthorizeUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authorizeUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Authorize Url"));
        return authorizeUrl;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Url"));
        return baseUrl;
    }
}
