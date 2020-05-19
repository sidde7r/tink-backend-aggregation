package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {

    @JsonProperty("j_gid_response_tgt")
    private String jGidResponseTgt;

    @JsonProperty("j_gid_response_ind_dl")
    private String jGidResponseIndDl;

    @JsonProperty("j_gid_response_contrato")
    private String jGidResponseContrato;

    @JsonProperty("j_gid_response_domain")
    private String jGidResponseDomain;

    @JsonProperty("j_gid_response_cod_error")
    private String jGidResponseCodError;

    @JsonProperty("j_gid_response_url")
    private String jGidResponseUrl;

    @JsonProperty("j_gid_response_lt")
    private String jGidResponseLt;

    @JsonProperty("j_gid_response_rsa")
    private String jGidResponseRsa;

    @JsonProperty("j_gid_response_error")
    private String jGidResponseError;

    @JsonProperty("version-gid-client")
    private String versionGidClient;

    public String getJGidResponseTgt() {
        return jGidResponseTgt;
    }

    public String getJGidResponseIndDl() {
        return jGidResponseIndDl;
    }

    public String getJGidResponseContrato() {
        return jGidResponseContrato;
    }

    public String getJGidResponseDomain() {
        return jGidResponseDomain;
    }

    public String getJGidResponseCodError() {
        return jGidResponseCodError;
    }

    public String getJGidResponseUrl() {
        return jGidResponseUrl;
    }

    public String getJGidResponseLt() {
        return jGidResponseLt;
    }

    public String getJGidResponseRsa() {
        return jGidResponseRsa;
    }

    public String getJGidResponseError() {
        return jGidResponseError;
    }

    public String getVersionGidClient() {
        return versionGidClient;
    }
}
