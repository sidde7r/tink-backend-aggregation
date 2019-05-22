package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerServiceInfo {

    @JsonProperty("openHours")
    private String openHours;

    @JsonProperty("serviceClass")
    private String serviceClass;

    @JsonProperty("phone")
    private String phone;
}
