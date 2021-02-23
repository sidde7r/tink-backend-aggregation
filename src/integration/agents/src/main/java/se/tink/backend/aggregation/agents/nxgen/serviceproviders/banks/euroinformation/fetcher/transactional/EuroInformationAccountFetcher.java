package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
public class EuroInformationAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    EuroInformationAccountFetcher(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationAccountFetcher create(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        return new EuroInformationAccountFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountSummaryResponse details =
                this.sessionStorage
                        .get(
                                EuroInformationConstants.Tags.ACCOUNT_LIST,
                                AccountSummaryResponse.class)
                        .orElseGet(this.apiClient::requestAccounts);

        return details.getAccountDetailsList().stream()
                .map(
                        accountDetailsEntity ->
                                accountDetailsEntity.toTinkAccount(
                                        getHolderForAccount(accountDetailsEntity)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Holder getHolderForAccount(AccountDetailsEntity accountDetailsEntity) {
        return this.sessionStorage
                .get(Storage.LOGIN_RESPONSE, LoginResponse.class)
                .map(LoginResponse::getClientName)
                .map(accountDetailsEntity::isHolder)
                .orElse(null);
    }
}
