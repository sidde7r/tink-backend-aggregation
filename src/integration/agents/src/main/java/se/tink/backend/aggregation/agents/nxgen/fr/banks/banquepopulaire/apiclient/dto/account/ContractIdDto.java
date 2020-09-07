package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class ContractIdDto {

    @JsonProperty("codeBanque")
    private String bankCode;

    @JsonProperty("identifiant")
    private String identifier;
}
