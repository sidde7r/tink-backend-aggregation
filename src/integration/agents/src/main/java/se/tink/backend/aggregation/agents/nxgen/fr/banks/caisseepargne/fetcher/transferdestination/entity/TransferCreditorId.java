package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transferdestination.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TransferCreditorId {

    @JsonProperty("entityCode")
    private String entityCode;

    @JsonProperty("id")
    private String id;
}
