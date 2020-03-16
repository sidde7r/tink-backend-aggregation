package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.http.url.URL;

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

    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;
    private final UkOpenBankingApiClient apiClient;
    private final Class<AccountResponseType> accountEntityType;
    private final Class<BalanceResponseType> balanceEntityType;
    private final AccountConverter<AccountResponseType, BalanceResponseType, AccountType>
            accountConverter;
    private final IdentityDataFetcher identityDataFetcher;

    public UkOpenBankingAccountFetcher(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            UkOpenBankingApiClient apiClient,
            Class<AccountResponseType> accountsResponseType,
            Class<BalanceResponseType> balancesResponseType,
            AccountConverter<AccountResponseType, BalanceResponseType, AccountType>
                    accountConverter,
            IdentityDataFetcher identityDataFetcher) {

        this.ukOpenBankingAisConfig = ukOpenBankingAisConfig;
        this.apiClient = apiClient;

        this.accountEntityType = accountsResponseType;
        this.balanceEntityType = balancesResponseType;

        this.accountConverter = accountConverter;
        this.identityDataFetcher = identityDataFetcher;
    }

    @Override
    public List<AccountType> fetchAccounts() {

        AccountResponseType accounts = apiClient.fetchAccounts(accountEntityType);

        URL identityDataEndpointURL = ukOpenBankingAisConfig.getIdentityDataURL();

        IdentityDataEntity identityData = null;

        if (Objects.nonNull(identityDataEndpointURL)) {
            identityData = fetchIdentityData(accounts, identityDataEndpointURL);
            ukOpenBankingAisConfig.setIdentityData(identityData);
        }
        // In order to keep the model simple we accept that we are revisiting the accounts list
        // multiple time.
        String partyName =
                Optional.ofNullable(identityData).map(IdentityDataEntity::getName).orElse(null);

        List<AccountType> tinkAccounts =
                accounts.stream()
                        .map(
                                account ->
                                        accountConverter.toTinkAccount(
                                                accounts,
                                                apiClient.fetchAccountBalance(
                                                        account.getBankIdentifier(),
                                                        balanceEntityType),
                                                partyName))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        HolderName holderName =
                tinkAccounts.stream()
                        .filter(e -> e.getHolderName() != null)
                        .map(AccountType::getHolderName)
                        .findAny()
                        .orElse(null);

        if (Objects.nonNull(holderName)) {
            ukOpenBankingAisConfig.setHolderName(holderName.toString());
        }

        return tinkAccounts;
    }

    private IdentityDataEntity fetchIdentityData(
            AccountResponseType accounts, URL identityDataEndpointURL) {
        if (identityDataEndpointURL.get().contains("accounts")) {
            return identityDataFetcher.fetchUserDetails(
                    ukOpenBankingAisConfig
                            .getApiBaseURL()
                            .concat(
                                    String.format(
                                            identityDataEndpointURL.get(),
                                            accounts.stream()
                                                    .findAny()
                                                    .get()
                                                    .getBankIdentifier())));

        } else {
            return identityDataFetcher.fetchUserDetails(
                    ukOpenBankingAisConfig.getApiBaseURL().concat(identityDataEndpointURL.get()));
        }
    }
}
