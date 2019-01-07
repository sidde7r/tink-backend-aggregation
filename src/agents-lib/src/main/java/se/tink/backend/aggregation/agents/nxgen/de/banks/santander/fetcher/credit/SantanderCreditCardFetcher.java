package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit;

import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class SantanderCreditCardFetcher
    implements AccountFetcher<CreditCardAccount>,
        TransactionKeyPaginator<CreditCardAccount, String> {

  private final SantanderApiClient client;

  public SantanderCreditCardFetcher(SantanderApiClient client) {
    this.client = client;
  }

  @Override
  public Collection<CreditCardAccount> fetchAccounts() {
    ArrayList<CreditCardAccount> res = new ArrayList<>();
    AccountsResponse response = client.fetchAccounts();
    if (!response.containsCreditCards()) {
      return res;
    }
    CardDetailsResponse creditInfoResponse =
        client.fetchCardInfo(response.getAccountResultEntity().getCreditPan());
    res.add(creditInfoResponse.toCreditCardAccount());
    return res;
  }

  @Override
  public TransactionKeyPaginatorResponse<String> getTransactionsFor(
      CreditCardAccount account, String key) {
    return client.fetchCardTransactions();
  }
}
