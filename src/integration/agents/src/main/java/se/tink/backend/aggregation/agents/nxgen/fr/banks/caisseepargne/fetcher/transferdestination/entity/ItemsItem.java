package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transferdestination.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class ItemsItem {

    @JsonProperty("identification")
    private Identification identification;

    @JsonProperty("transferCreditorIdentity")
    private TransferCreditorIdentity transferCreditorIdentity;
}
