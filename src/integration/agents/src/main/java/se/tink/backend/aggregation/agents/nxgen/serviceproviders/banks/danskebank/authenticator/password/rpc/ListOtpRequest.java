package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListOtpRequest {
    @JsonProperty("UserIdType")
    private final String userIdType;

    private ListOtpRequest(String userIdType) {
        this.userIdType = userIdType;
    }

    public static ListOtpRequest create(String userIdType) {
        return new ListOtpRequest(userIdType);
    }

    public String getUserIdType() {
        return userIdType;
    }
}
