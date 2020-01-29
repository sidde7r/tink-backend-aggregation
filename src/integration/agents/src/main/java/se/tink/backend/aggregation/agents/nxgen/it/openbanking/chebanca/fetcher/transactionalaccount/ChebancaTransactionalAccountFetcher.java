package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.ACCOUNTS_FETCH_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.BALANCES_FETCH_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.CONSENT_AUTHORIZATION_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.CONSENT_CONFIRMATION_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.CONSENT_CREATION_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.GET_CUSTOMER_ID_FAILED;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.HttpResponseChecker;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail.TransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.BalancesDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ChebancaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final ChebancaApiClient apiClient;
    private final ChebancaConsentManualApproveController consentController;

    public ChebancaTransactionalAccountFetcher(
            ChebancaApiClient apiClient, ChebancaConsentManualApproveController consentController) {
        this.apiClient = requireNonNull(apiClient);
        this.consentController = consentController;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        fetchAndSaveCustomerId();
        HttpResponse httpResponse = apiClient.getAccounts();
        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, ACCOUNTS_FETCH_FAILED);
        GetAccountsResponse getAccountsResponse = httpResponse.getBody(GetAccountsResponse.class);

        List<String> accountIds =
                getAccountsResponse.getData().getAccounts().stream()
                        .map(AccountEntity::getAccountId)
                        .collect(Collectors.toList());

        processConsent(accountIds);

        return getAccountsResponse.getData().getAccounts().stream()
                .filter(TransactionalAccountMapper::isAccountOfInterest)
                .map(this::toTinkAccount)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void fetchAndSaveCustomerId() {
        HttpResponse httpResponse = apiClient.getCustomerId();
        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, GET_CUSTOMER_ID_FAILED);
        CustomerIdResponse customerIdResponse = httpResponse.getBody(CustomerIdResponse.class);
        apiClient.save(StorageKeys.CUSTOMER_ID, customerIdResponse.getData().getCustomerid());
    }

    private void processConsent(List<String> accountIds) {
        ConsentResponse consentResponse = createConsent(accountIds);
        String scaRedirectUrl = authorizeConsent(consentResponse);
        consentController.approveConsentByUser(scaRedirectUrl);
        confirmConsent(consentResponse);
    }

    private ConsentResponse createConsent(List<String> accountIds) {
        List<CategoryEntity> categoryEntities =
                Collections.singletonList(new CategoryEntity(FormValues.ACCOUNT_INFO, accountIds));

        ConsentRequest consentRequest = new ConsentRequest(new ConsentDataEntity(categoryEntities));

        HttpResponse httpResponse = apiClient.createConsent(consentRequest);
        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, CONSENT_CREATION_FAILED);

        return httpResponse.getBody(ConsentResponse.class);
    }

    private String authorizeConsent(ConsentResponse consentResponse) {
        HttpResponse httpResponse =
                apiClient.authorizeConsent(consentResponse.getResources().getResourceId());

        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, CONSENT_AUTHORIZATION_FAILED);

        ConsentAuthorizationResponse consentAuthorizationResponse =
                httpResponse.getBody(ConsentAuthorizationResponse.class);

        return consentAuthorizationResponse.getData().getScaRedirectURL();
    }

    private void confirmConsent(ConsentResponse consentResponse) {
        HttpResponse httpResponse =
                apiClient.confirmConsent(consentResponse.getResources().getResourceId());
        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, CONSENT_CONFIRMATION_FAILED);
    }

    private Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        HttpResponse response = apiClient.getBalances(accountEntity.getAccountId());
        HttpResponseChecker.checkIfSuccessfulResponse(
                response, HttpServletResponse.SC_OK, BALANCES_FETCH_FAILED);

        return TransactionalAccountMapper.mapToTinkAccount(
                accountEntity, getAmountEntity(response));
    }

    private AmountEntity getAmountEntity(HttpResponse response) {
        GetBalancesResponse balances = response.getBody(GetBalancesResponse.class);
        return Optional.of(balances)
                .map(GetBalancesResponse::getData)
                .map(BalancesDataEntity::getAvailableBalance)
                .orElseThrow(
                        () ->
                                new RequiredDataMissingException(
                                        "No information about balance is available"));
    }
}
