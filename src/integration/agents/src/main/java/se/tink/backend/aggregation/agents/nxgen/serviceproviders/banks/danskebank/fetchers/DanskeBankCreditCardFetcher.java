package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardsListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class DanskeBankCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final AccountEntityMapper accountEntityMapper;
    private final DanskeBankAccountDetailsFetcher accountDetailsFetcher;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<AccountEntity> cardAccounts =
                apiClient
                        .listAccounts(
                                ListAccountsRequest.createFromLanguageCode(
                                        configuration.getLanguageCode()))
                        .getAccounts().stream()
                        .filter(DanskeBankPredicates.CREDIT_CARDS)
                        .collect(Collectors.toList());

        Map<String, AccountDetailsResponse> accountDetails = new HashMap<>();
        for (AccountEntity accountEntity : cardAccounts) {
            accountDetails.put(
                    accountEntity.getAccountNoExt(),
                    accountDetailsFetcher.fetchAccountDetails(accountEntity.getAccountNoInt()));
        }

        reachCardEndpointsForDebugPurposes();

        return accountEntityMapper.toTinkCreditCardAccounts(
                configuration, cardAccounts, accountDetails);
    }

    private void reachCardEndpointsForDebugPurposes() {
        this.apiClient.listCards(CardsListRequest.create(configuration.getLanguageCode()));
    }
}
