package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.creditcards;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SibsCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {

    private final SibsBaseApiClient apiClient;

    public SibsCreditCardFetcher(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        AccountsResponse accountsResponse = apiClient.fetchAccounts();
        return accountsResponse.getAccountList().stream()
                .map(this::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    private CreditCardAccount toTinkCreditCard(AccountEntity accountEntity) {
        ExactCurrencyAmount balanceAmount =
                apiClient.getAccountBalances(accountEntity.getId()).getBalances().stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SibsConstants.ErrorMessages.NO_BALANCE))
                        .getInterimAvailable()
                        .getAmount()
                        .toTinkAmount();

        return accountEntity.toTinkCreditCard(balanceAmount);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
