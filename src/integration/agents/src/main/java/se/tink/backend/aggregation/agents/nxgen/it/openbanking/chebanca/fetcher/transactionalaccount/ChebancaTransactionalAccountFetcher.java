package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ChebancaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final ChebancaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;
    private final Credentials credentials;

    public ChebancaTransactionalAccountFetcher(
            ChebancaApiClient apiClient,
            PersistentStorage persistentStorage,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.thirdPartyAppAuthenticationController = thirdPartyAppAuthenticationController;
        this.credentials = credentials;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        CustomerIdResponse customerIdResponse = apiClient.getCustomerId();
        persistentStorage.put(
                StorageKeys.CUSTOMER_ID, customerIdResponse.getData().getCustomerid());

        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        List<String> accountIds =
                getAccountsResponse.getData().getAccounts().stream()
                        .map(AccountEntity::getAccountId)
                        .collect(Collectors.toList());

        List<CategoryEntity> categoryEntities =
                Collections.singletonList(new CategoryEntity(FormValues.ACCOUNT_INFO, accountIds));

        ConsentRequest consentRequest = new ConsentRequest(new ConsentDataEntity(categoryEntities));

        ConsentResponse consentResponse = apiClient.createConsent(consentRequest);
        ConsentAuthorizationResponse consentAuthorizationResponse =
                apiClient.consentAuthorization(consentResponse.getResources().getResourceId());

        persistentStorage.put(
                StorageKeys.AUTHORIZATION_URL,
                consentAuthorizationResponse.getData().getScaRedirectURL());

        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);
        } catch (Exception e) {
            e.printStackTrace();
        }

        apiClient.confirmConsent(consentResponse.getResources().getResourceId());

        return getAccountsResponse.getData().getAccounts().stream()
                .filter(AccountEntity::isCheckingAccount)
                .map(
                        accountEntity ->
                                accountEntity.toTinkAccount(
                                        apiClient
                                                .getBalances(accountEntity.getAccountId())
                                                .getData()
                                                .getAvailableBalance()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
