package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.PSU_CREDENTIALS_INVALID;

import java.time.LocalDate;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SparkassenApiClient {

    private final TinkHttpClient client;
    private final String bankCode;
    private final boolean isManual;
    private final String userIp;

    public SparkassenApiClient(
            TinkHttpClient client, String bankCode, boolean isManual, String userIp) {
        this.client = client;
        this.bankCode = bankCode;
        this.isManual = isManual;
        this.userIp = userIp;
    }

    private RequestBuilder createRequest(URL url) {
        if (url.get().contains("{" + PathVariables.BANK_CODE + "}")) {
            url = url.parameter(PathVariables.BANK_CODE, bankCode);
        }

        RequestBuilder requestBuilder =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID());

        return isManual ? requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, userIp) : requestBuilder;
    }

    private RequestBuilder createRequestInSession(URL url, String consentId) {
        return createRequest(url).header(HeaderKeys.CONSENT_ID, consentId);
    }

    public ConsentResponse createConsent() throws LoginException {
        LocalDate validUntil = LocalDate.now().plusDays(90);
        ConsentRequest consentRequest =
                new ConsentRequest(
                        new AccessEntity(),
                        true,
                        validUntil.toString(),
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        ConsentResponse consentResponse =
                createRequest(Urls.CONSENT)
                        .header(HeaderKeys.TPP_REDIRECT_PREFERRED, false)
                        .post(ConsentResponse.class, consentRequest);
        consentResponse.setValidUntil(validUntil);
        return consentResponse;
    }

    public AuthenticationMethodResponse initializeAuthorization(
            URL url, String username, String password) throws AuthenticationException {
        try {
            return createRequest(url)
                    .header(HeaderKeys.PSU_ID, username)
                    .post(
                            AuthenticationMethodResponse.class,
                            new InitAuthorizationRequest(new PsuDataEntity(password)));
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(String.class).contains(PSU_CREDENTIALS_INVALID)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }
            throw e;
        }
    }

    public AuthenticationMethodResponse selectAuthorizationMethod(
            String consentId, String authorizationId, String methodId) {
        return createRequest(
                        Urls.UPDATE_SCA_METHOD
                                .parameter(PathVariables.CONSENT_ID, consentId)
                                .parameter(PathVariables.AUTHORIZATION_ID, authorizationId))
                .put(
                        AuthenticationMethodResponse.class,
                        new SelectAuthenticationMethodRequest(methodId));
    }

    public FinalizeAuthorizationResponse finalizeAuthorization(
            String consentId, String authorizationId, String otp) {
        try {
            return createRequest(
                            Urls.FINALIZE_AUTHORIZATION
                                    .parameter(PathVariables.CONSENT_ID, consentId)
                                    .parameter(PathVariables.AUTHORIZATION_ID, authorizationId))
                    .put(
                            FinalizeAuthorizationResponse.class,
                            new FinalizeAuthorizationRequest(otp));
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(String.class).contains(PSU_CREDENTIALS_INVALID)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
            }
            throw e;
        }
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequest(Urls.CONSENT_STATUS.parameter(PathVariables.CONSENT_ID, consentId))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(ConsentStatusResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return createRequest(Urls.CONSENT_DETAILS.parameter(PathVariables.CONSENT_ID, consentId))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(ConsentDetailsResponse.class);
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
