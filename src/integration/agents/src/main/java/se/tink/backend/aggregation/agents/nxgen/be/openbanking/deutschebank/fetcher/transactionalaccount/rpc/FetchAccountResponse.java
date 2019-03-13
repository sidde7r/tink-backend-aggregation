package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {

  private List<AccountsEntity> accounts;

  public Collection<TransactionalAccount> toTinkAccounts(String owner) {
    return accounts != null
        ? accounts.stream()
            .map(a -> a.toTinkAccount(owner))
            .collect(Collectors.toList())
        : Collections.emptyList();
  }
}
