package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.executor.beneficiary.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Balance{

    @JsonProperty("value")
    private Object value;

    @JsonProperty("currencyCode")
    private String currencyCode;
}