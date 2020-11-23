package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardsListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class DanskeBankCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final AccountEntityMapper accountEntityMapper;

    public DanskeBankCreditCardFetcher(
            DanskeBankApiClient apiClient,
            DanskeBankConfiguration configuration,
            AccountEntityMapper accountEntityMapper) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.accountEntityMapper = accountEntityMapper;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        ListAccountsResponse listAccountsResponse =
                this.apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        reachCardEndpointsForDebugPurposes();

        return accountEntityMapper.toTinkCreditCardAccounts(
                configuration, listAccountsResponse.getAccounts());
    }

    private void reachCardEndpointsForDebugPurposes() {
        this.apiClient.listCards(CardsListRequest.create(configuration.getLanguageCode()));
    }
}
