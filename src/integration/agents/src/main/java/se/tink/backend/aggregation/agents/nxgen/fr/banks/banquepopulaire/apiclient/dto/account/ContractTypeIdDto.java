package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonObject
public class ContractTypeIdDto extends ContractIdDto {

    @JsonProperty("typeContrat")
    private String typeContract;
}
