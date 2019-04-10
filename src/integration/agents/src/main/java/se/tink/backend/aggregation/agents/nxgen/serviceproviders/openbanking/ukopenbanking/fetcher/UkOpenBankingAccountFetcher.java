package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;

/**
 * Generic account fetcher for ukob.
 *
 * @param <AccountResponseType> The account response entity
 * @param <BalanceResponseType> The account balance response entity
 * @param <AccountType> The type of account to fetch. eg. TransactionalAccount, CreditCard, etc.
 */
public class UkOpenBankingAccountFetcher<
                AccountResponseType extends AccountStream,
                BalanceResponseType,
                AccountType extends Account>
        implements AccountFetcher<AccountType> {

    private final UkOpenBankingApiClient apiClient;
    private final Class<AccountResponseType> accountEntityType;
    private final Class<BalanceResponseType> balanceEntityType;
    private final AccountConverter<AccountResponseType, BalanceResponseType, AccountType>
            accountConverter;

    /**
     * @param apiClient Ukob api client
     * @param accountsResponseType Class type of the account response entity
     * @param balancesResponseType Class type of the account balance response entity
     * @param accountConverter A method taking the AccountEntity and AccountBalanceEntity and
     *     converting it to a Tink account. See: {@link
     *     se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.AccountConverter#toTinkAccount(Object,
     *     Object)}
     */
    public UkOpenBankingAccountFetcher(
            UkOpenBankingApiClient apiClient,
            Class<AccountResponseType> accountsResponseType,
            Class<BalanceResponseType> balancesResponseType,
            AccountConverter<AccountResponseType, BalanceResponseType, AccountType>
                    accountConverter) {

        this.apiClient = apiClient;

        this.accountEntityType = accountsResponseType;
        this.balanceEntityType = balancesResponseType;

        this.accountConverter = accountConverter;
    }

    @Override
    public List<AccountType> fetchAccounts() {

        AccountResponseType accounts = apiClient.fetchAccounts(accountEntityType);

        // In order to keep the model simple we accept that we are revisiting the accounts list
        // multiple time.
        return accounts.stream()
                .map(
                        account ->
                                accountConverter.toTinkAccount(
                                        accounts,
                                        apiClient.fetchAccountBalance(
                                                account.getBankIdentifier(), balanceEntityType)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
