package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class UkOpenBankingConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String organizationId;
    @JsonProperty @SensitiveSecret private String clientId;
    @JsonProperty @Secret private String signingKey;
    @JsonProperty @Secret private String signingKeyId;
    @JsonProperty @Secret private String softwareStatementAssertion;
    @JsonProperty @Secret private String redirectUrl;
    @JsonProperty @Secret private String softwareId;
    @JsonProperty @Secret private String transportKey;
    @JsonProperty @Secret private String transportKeyId;
    @JsonProperty @Secret private String rootCAData;
    @JsonProperty @SensitiveSecret private String clientSecret;
    @JsonProperty @SensitiveSecret private String transportKeyPassword;
    @JsonProperty @SensitiveSecret private String signingKeyPassword;
    @JsonProperty @SensitiveSecret private String rootCAPassword;

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
