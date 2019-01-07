package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class SantanderAccountFetcher implements AccountFetcher<TransactionalAccount> {

  private final SantanderApiClient client;

  public SantanderAccountFetcher(SantanderApiClient client) {
    this.client = client;
  }

  @Override
  public Collection<TransactionalAccount> fetchAccounts() {
    return client.fetchAccounts().toTransactionalAccounts();
  }
}
