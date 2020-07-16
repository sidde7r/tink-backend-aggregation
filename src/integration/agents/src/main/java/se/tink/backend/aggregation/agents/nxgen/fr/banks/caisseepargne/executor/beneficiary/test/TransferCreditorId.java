package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferCreditorId {

    @JsonProperty("entityCode")
    private String entityCode;

    @JsonProperty("id")
    private String id;
}
