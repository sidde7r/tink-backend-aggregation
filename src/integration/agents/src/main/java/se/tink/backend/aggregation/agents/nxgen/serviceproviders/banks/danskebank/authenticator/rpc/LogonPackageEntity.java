package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogonPackageEntity {
    @JsonProperty("UserID")
    private String userId;

    @JsonProperty("LogonPackage")
    private String logonPackage;

    public String getUserId() {
        return userId;
    }

    public String getLogonPackage() {
        return logonPackage;
    }
}
