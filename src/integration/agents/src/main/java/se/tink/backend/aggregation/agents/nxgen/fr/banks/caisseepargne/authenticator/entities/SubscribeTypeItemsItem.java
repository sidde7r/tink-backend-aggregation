package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SubscribeTypeItemsItem {

    @JsonProperty("code")
    private String code;

    @JsonProperty("label")
    private String label;
}
