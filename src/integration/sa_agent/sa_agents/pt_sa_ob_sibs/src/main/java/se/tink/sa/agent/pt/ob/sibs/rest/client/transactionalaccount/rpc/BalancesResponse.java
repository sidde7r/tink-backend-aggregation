package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.BalanceEntity;

@Getter
@Setter
public class BalancesResponse {

    private List<BalanceEntity> balances;
}
