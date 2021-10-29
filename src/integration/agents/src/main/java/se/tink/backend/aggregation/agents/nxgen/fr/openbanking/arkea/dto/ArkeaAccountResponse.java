package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaAccountLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaAccountResponse {

    @JsonProperty("accounts")
    private List<ArkeaAccountEntity> accountEntityList;

    @JsonProperty("_links")
    private ArkeaAccountLinksEntity accountLinksEntity;
}
