package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreditorType {

    @JsonProperty("code")
    private String code;

    @JsonProperty("label")
    private String label;
}
