package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ICSConfiguration {
    @JsonProperty
    private String clientId;
    @JsonProperty
    private String clientSecret;
    @JsonProperty
    private String redirectUri;
    @JsonProperty
    private String clientSSLCertificate;
    @JsonProperty
    private String rootCACertificate;
    @JsonProperty
    private String rootCAPassword;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getClientSSLCertificate() {
        return clientSSLCertificate;
    }

    public String getRootCACertificate() {
        return rootCACertificate;
    }

    public String getRootCAPassword() {
        return rootCAPassword;
    }

    public boolean isEmpty() {
        return !Strings.isNullOrEmpty(clientId) &&
                !Strings.isNullOrEmpty(clientSecret) &&
                !Strings.isNullOrEmpty(redirectUri) &&
                !Strings.isNullOrEmpty(clientSSLCertificate) &&
                !Strings.isNullOrEmpty(rootCAPassword) &&
                !Strings.isNullOrEmpty(rootCAPassword);
    }
}
