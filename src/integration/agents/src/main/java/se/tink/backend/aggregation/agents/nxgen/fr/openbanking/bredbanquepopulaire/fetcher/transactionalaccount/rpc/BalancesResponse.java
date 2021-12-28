package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.BalancesLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BalancesResponse {
    List<BalanceEntity> balances;

    @JsonProperty("_links")
    private BalancesLinksEntity balancesLinksEntity;
}
