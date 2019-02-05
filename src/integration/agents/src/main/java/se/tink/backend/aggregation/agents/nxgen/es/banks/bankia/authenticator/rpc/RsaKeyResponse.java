package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RsaKeyResponse {
    @JsonProperty("j_gid_response_rsa")
    private String rsaPublicKey;
    @JsonProperty("j_gid_response_url")
    private String responseUrl;
    @JsonProperty("j_gid_response_domain")
    private String responseDomain;
    @JsonProperty("j_gid_response_lt")
    private String responseLt;
    @JsonProperty("version-gid-client")
    private String versionGidClient;
    @JsonProperty("j_gid_response_original_request")
    private String responseOriginalRequest;

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public String getResponseDomain() {
        return responseDomain;
    }

    public String getResponseLt() {
        return responseLt;
    }

    public String getVersionGidClient() {
        return versionGidClient;
    }

    public String getResponseOriginalRequest() {
        return responseOriginalRequest;
    }
}
