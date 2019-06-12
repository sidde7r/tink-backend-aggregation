package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountResponse {
  private List<AccountEntity> accounts;

  public AccountsBaseResponse toBerlingGroupAccountBaseResponse(){
    List<AccountBaseEntity> accountBaseEntities =
        Optional.ofNullable(accounts).orElse(Collections.emptyList())
        .stream()
        .map(account->account.toBerlinGroupAccountBaseResponse())
        .collect(Collectors.toList());
    return new AccountsBaseResponse(accountBaseEntities);
  }

  public List<AccountEntity> getAccounts() {return accounts; }
}
