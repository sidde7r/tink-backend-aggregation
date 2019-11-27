package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc;

import lombok.Getter;
import lombok.Setter;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;

@Getter
@Setter
public class AccountDetailsResponse {

    private AccountEntity account;
}
