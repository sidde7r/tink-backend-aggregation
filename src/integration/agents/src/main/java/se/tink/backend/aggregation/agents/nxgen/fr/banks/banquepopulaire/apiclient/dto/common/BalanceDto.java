package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class BalanceDto {

    @JsonProperty("valeur")
    private String amount;

    @JsonProperty("devise")
    private String currency;
}
