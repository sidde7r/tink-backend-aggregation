package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyCardLoginResponse extends SpankkiResponse {
    @JsonProperty private CustomerEntity customer;
    @JsonProperty private String loginToken = "";
    @JsonProperty private String deviceId = "";

    @JsonIgnore
    public CustomerEntity getCustomer() {
        return customer;
    }

    @JsonIgnore
    public String getDeviceId() {
        return deviceId;
    }

    @JsonIgnore
    public String getLoginToken() {
        return loginToken;
    }
}
