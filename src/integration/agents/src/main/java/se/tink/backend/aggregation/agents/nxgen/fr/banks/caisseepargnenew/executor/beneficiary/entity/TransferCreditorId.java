package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.executor.beneficiary.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TransferCreditorId {
    @JsonProperty("entityCode")
    private String entityCode;

    @JsonProperty("id")
    private String id;
}
