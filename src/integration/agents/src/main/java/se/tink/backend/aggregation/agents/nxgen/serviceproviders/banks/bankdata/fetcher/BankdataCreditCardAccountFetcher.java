package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class BankdataCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final BankdataApiClient bankClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return BankdataCreditCardAccountMapper.getCreditCardAccounts(bankClient.getAccounts());
    }
}
