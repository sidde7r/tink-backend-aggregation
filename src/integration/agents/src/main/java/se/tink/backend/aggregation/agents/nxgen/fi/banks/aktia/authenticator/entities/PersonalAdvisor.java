package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonalAdvisor {

    @JsonProperty("advisorServiceClass")
    private String advisorServiceClass;

    @JsonProperty("phone")
    private Object phone;

    @JsonProperty("name")
    private Object name;
}
