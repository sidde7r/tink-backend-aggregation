package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Balance {

    @JsonProperty("value")
    private Object value;

    @JsonProperty("currencyCode")
    private String currencyCode;
}
