package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Identification {

    @JsonProperty("transferCreditorId")
    private TransferCreditorId transferCreditorId;
}
