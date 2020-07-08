package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class TransferCategoryType {

    @JsonProperty("code")
    private String code;
}
