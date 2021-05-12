package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.ACCOUNTS_FETCH_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.CONSENT_AUTHORIZATION_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.CONSENT_CONFIRMATION_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.CONSENT_CREATION_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.GET_CUSTOMER_ID_FAILED;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.HttpResponseChecker;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.DateFormat;

@RequiredArgsConstructor
public class ChebancaConsentManager {
    private final ChebancaApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;

    public void processConsent() {
        fetchAndSaveCustomerId();

        // Chebanca need account Ids for requesting a consent. Hence calling for account during
        // authentication is needed
        GetAccountsResponse getAccountsResponse = fetchAccountResponse();

        List<String> accountIds =
                getAccountsResponse.getData().getAccounts().stream()
                        .map(AccountEntity::getAccountId)
                        .collect(Collectors.toList());
        ConsentResponse consentResponse = createConsent(accountIds);
        String scaRedirectUrl = authorizeConsent(consentResponse);
        approveConsentByUser(scaRedirectUrl);
        confirmConsent(consentResponse);

        // Chebanca doesn't support rocket technology API with endpoint for consent status and
        // expiration date. So it has to be set manually.
        setSessionExpiryDate();
    }

    private void setSessionExpiryDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime consentExpirationDate = now.plusDays(89);
        credentials.setSessionExpiryDate(
                DateFormat.convertToDateViaInstant(consentExpirationDate.toLocalDate()));
    }

    public boolean isConsentValid() {
        Date date = credentials.getSessionExpiryDate();
        return date != null && date.after(new Date());
    }

    private void fetchAndSaveCustomerId() {
        HttpResponse httpResponse = apiClient.getCustomerId();
        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, GET_CUSTOMER_ID_FAILED);
        CustomerIdResponse customerIdResponse = httpResponse.getBody(CustomerIdResponse.class);
        apiClient.save(StorageKeys.CUSTOMER_ID, customerIdResponse.getData().getCustomerId());
    }

    private GetAccountsResponse fetchAccountResponse() {
        HttpResponse httpResponse = apiClient.getAccounts();
        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, ACCOUNTS_FETCH_FAILED);
        return httpResponse.getBody(GetAccountsResponse.class);
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

    ThirdPartyAppResponse<String> approveConsentByUser(String url) {
        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(new URL(url)));
        supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }
}
