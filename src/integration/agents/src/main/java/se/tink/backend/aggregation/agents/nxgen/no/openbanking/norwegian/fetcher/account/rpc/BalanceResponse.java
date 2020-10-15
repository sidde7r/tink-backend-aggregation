package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.BalancesItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalanceResponse {

    private List<BalancesItemEntity> balances;
}
