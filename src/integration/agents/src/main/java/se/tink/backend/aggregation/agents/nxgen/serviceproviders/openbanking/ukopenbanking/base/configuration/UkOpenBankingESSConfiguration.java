package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class UkOpenBankingESSConfiguration implements ClientConfiguration {
    private String organizationId;
    private String clientId;
    private String signingKey;
    private String signingKeyId;
    private String softwareStatementAssertion;
    private String redirectUrl;
    private String softwareId;
    private String transportKey;
    private String transportKeyId;
    private String rootCAData;
    private String clientSecret;
    private String transportKeyPassword;
    private String signingKeyPassword;
    private String rootCAPassword;

    public String getOrganizationId() {
        return organizationId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public String getSigningKeyId() {
        return signingKeyId;
    }

    public String getSoftwareStatementAssertion() {
        return softwareStatementAssertion;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public String getTransportKey() {
        return transportKey;
    }

    public String getTransportKeyId() {
        return transportKeyId;
    }

    public byte[] getRootCAData() {
        return EncodingUtils.decodeBase64String(rootCAData);
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTransportKeyPassword() {
        return transportKeyPassword;
    }

    public String getSigningKeyPassword() {
        return signingKeyPassword;
    }

    public String getRootCAPassword() {
        return rootCAPassword;
    }
}
