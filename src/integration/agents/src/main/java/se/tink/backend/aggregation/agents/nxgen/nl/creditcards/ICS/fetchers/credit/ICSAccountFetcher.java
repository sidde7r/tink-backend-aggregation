package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit;

import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class ICSAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final ICSApiClient client;

    public ICSAccountFetcher(ICSApiClient client) {
        this.client = client;
    }

    private Collection<CreditCardAccount> toCreditCardAccounts(
            CreditAccountsResponse accountsResponse) {
        ArrayList<CreditCardAccount> result = new ArrayList<>();
        for (AccountEntity account : accountsResponse.getData().getAccount()) {
            if (account.getCreditCardEntity().isActive()) {
                CreditBalanceResponse balanceResponse =
                        this.client.getAccountBalance(account.getAccountId());
                result.add(account.toCreditCardAccount(balanceResponse));
            }
        }
        return result;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        CreditAccountsResponse accountsResponse = this.client.getAllAccounts();
        return toCreditCardAccounts(accountsResponse);
    }
}
