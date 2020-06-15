package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.IBAN_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.PSU_CREDENTIALS_INVALID;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.IbanAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SparkassenApiClient {

    private final TinkHttpClient client;
    private final Credentials credentials;
    private final String bankCode;

    public SparkassenApiClient(TinkHttpClient client, Credentials credentials, String bankCode) {
        this.client = client;
        this.credentials = credentials;
        this.bankCode = bankCode;
    }

    private RequestBuilder createRequest(URL url) {
        if (url.get().contains("{" + PathVariables.BANK_CODE + "}")) {
            url = url.parameter(PathVariables.BANK_CODE, bankCode);
        }
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID());
    }

    private RequestBuilder createRequestInSession(URL url, String consentId) {
        return createRequest(url).header(HeaderKeys.CONSENT_ID, consentId);
    }

    public ConsentResponse createConsent(List<String> ibans) throws LoginException {
        List<AccountsEntity> accountsEntities =
                ibans.stream()
                        .map(String::trim)
                        .map(AccountsEntity::new)
                        .collect(Collectors.toList());

        ConsentRequest getConsentRequest =
                new ConsentRequest(
                        new IbanAccessEntity(accountsEntities, accountsEntities),
                        true,
                        LocalDate.now().plusDays(90).toString(),
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        try {
            return createRequest(Urls.GET_CONSENT)
                    .header(HeaderKeys.TPP_REDIRECT_PREFERRED, false)
                    .post(ConsentResponse.class, getConsentRequest);
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(String.class).contains(IBAN_INVALID)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw e;
        }
    }

    public InitAuthorizationResponse initializeAuthorization(
            URL url, String username, String password) throws AuthenticationException {
        try {
            return createRequest(url)
                    .header(HeaderKeys.PSU_ID, username)
                    .post(
                            InitAuthorizationResponse.class,
                            new InitAuthorizationRequest(new PsuDataEntity(password)));
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(String.class).contains(PSU_CREDENTIALS_INVALID)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw e;
        }
    }

    public SelectAuthenticationMethodResponse selectAuthorizationMethod(
            String consentId, String authorizationId, String methodId) {
        return createRequest(
                        Urls.UPDATE_SCA_METHOD
                                .parameter(PathVariables.CONSENT_ID, consentId)
                                .parameter(PathVariables.AUTHORIZATION_ID, authorizationId))
                .put(
                        SelectAuthenticationMethodResponse.class,
                        new SelectAuthenticationMethodRequest(methodId));
    }

    public FinalizeAuthorizationResponse finalizeAuthorization(
            String consentId, String authorizationId, String otp) {
        return createRequest(
                        Urls.FINALIZE_AUTHORIZATION
                                .parameter(PathVariables.CONSENT_ID, consentId)
                                .parameter(PathVariables.AUTHORIZATION_ID, authorizationId))
                .put(FinalizeAuthorizationResponse.class, new FinalizeAuthorizationRequest(otp));
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequest(
                        Urls.CHECK_CONSENT_STATUS.parameter(PathVariables.CONSENT_ID, consentId))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(ConsentStatusResponse.class);
    }

    public FetchAccountsResponse fetchAccounts(String consentId) {
        return createRequestInSession(SparkassenConstants.Urls.FETCH_ACCOUNTS, consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse getAccountBalance(String consentId, String accountId) {
        return createRequestInSession(
                        Urls.FETCH_BALANCES.parameter(PathVariables.ACCOUNT_ID, accountId),
                        consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchBalancesResponse.class);
    }

    public String fetchTransactions(String consentId, String accountId, LocalDate startDate) {
        return createRequestInSession(
                        Urls.FETCH_TRANSACTIONS
                                .parameter(PathVariables.ACCOUNT_ID, accountId)
                                .queryParam(QueryKeys.DATE_FROM, startDate.toString())
                                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH),
                        consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);
    }
}
