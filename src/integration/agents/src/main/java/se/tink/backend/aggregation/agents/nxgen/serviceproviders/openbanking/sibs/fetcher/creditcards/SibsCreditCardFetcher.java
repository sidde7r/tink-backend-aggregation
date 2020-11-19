package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.creditcards;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.SibsBaseTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsCreditCardFetcher extends SibsBaseTransactionFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, String> {

    public SibsCreditCardFetcher(
            SibsBaseApiClient apiClient,
            CredentialsRequest credentialsRequest,
            SibsUserState userState) {
        super(apiClient, credentialsRequest, userState);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        AccountsResponse accountsResponse = apiClient.fetchAccounts();
        return accountsResponse.getAccountList().stream()
                .map(this::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    private CreditCardAccount toTinkCreditCard(AccountEntity accountEntity) {

        BalanceEntity balanceEntity =
                apiClient.getAccountBalances(accountEntity.getId()).getBalances().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(ErrorMessages.NO_BALANCE));

        ExactCurrencyAmount closingBooked =
                balanceEntity.getClosingBooked().getAmount().toTinkAmount();

        ExactCurrencyAmount interimAvailable =
                balanceEntity.getInterimAvailable().getAmount().toTinkAmount();

        return accountEntity.toTinkCreditCard(closingBooked, interimAvailable);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        if (StringUtils.isNotEmpty(key)) {
            key = key.replace(StringUtils.SPACE, ENCODED_SPACE);
        }
        return Optional.ofNullable(key)
                .map(apiClient::getTransactionsForKey)
                .orElseGet(
                        () ->
                                apiClient.getAccountTransactions(
                                        account, getTransactionsFetchBeginDate(account)));
    }
}
