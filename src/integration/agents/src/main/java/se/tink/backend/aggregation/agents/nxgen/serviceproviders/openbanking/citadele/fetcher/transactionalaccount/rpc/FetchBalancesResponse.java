package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FetchBalancesResponse {

    private List<BalanceEntity> balances;
}
