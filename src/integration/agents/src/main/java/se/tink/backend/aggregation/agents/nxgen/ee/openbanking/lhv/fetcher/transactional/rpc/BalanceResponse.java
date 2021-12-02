package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities.AccountNumberEntity;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BalanceResponse {
    private AccountNumberEntity account;
    private List<BalanceEntity> balances;
}
