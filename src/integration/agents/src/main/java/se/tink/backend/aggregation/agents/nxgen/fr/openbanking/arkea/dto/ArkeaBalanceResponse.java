package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaBalanceLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaBalanceResponse {

    @JsonProperty("balances")
    private List<ArkeaBalanceEntity> balanceEntityList;

    @JsonProperty("_links")
    private ArkeaBalanceLinksEntity balanceLinksEntity;
}
