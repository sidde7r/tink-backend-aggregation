package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import java.time.LocalDate;
import java.util.List;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler.ErrorSource;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.PsuDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.SelectAuthorizationMethodRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class BankverlagApiClient {

    private final BankverlagRequestBuilder requestBuilder;
    private final BankverlagStorage storage;
    private final LocalDateTimeSource localDateTimeSource;
    private final BankverlagErrorHandler errorHandler;

    private RequestBuilder createRequestInSession(URL url, String consentId) {
        return requestBuilder.createRequest(url).header(HeaderKeys.CONSENT_ID, consentId);
    }

    private RequestBuilder createRequestInSessionWithTypes(
            URL url, String consentId, MediaType acceptHeader, MediaType type) {
        return requestBuilder
                .createRequest(url, acceptHeader, type)
                .header(HeaderKeys.CONSENT_ID, consentId);
    }

    public ConsentResponse createConsent() {
        ConsentRequest consentRequest =
                ConsentRequest.buildTypicalRecurring(
                        AccessEntity.builder().allPsd2(AccessType.ALL_ACCOUNTS).build(),
                        localDateTimeSource);

        return requestBuilder
                .createRequest(Urls.CONSENT)
                .post(ConsentResponse.class, consentRequest);
    }

    public AuthorizationResponse initializeAuthorization(
            String url, String username, String password) {

        try {
            HttpResponse response =
                    requestBuilder
                            .createRequest(new URL(url))
                            .header(HeaderKeys.PSU_ID, username)
                            .post(
                                    HttpResponse.class,
                                    new AuthorizationRequest(new PsuDataEntity(password)));

            List<String> scaApproachHeaders = response.getHeaders().get(HeaderKeys.ASPSP_APPROACH);

            if (scaApproachHeaders != null
                    && scaApproachHeaders.get(0).equalsIgnoreCase("decoupled")) {
                storage.savePushOtpFromHeader();
            }

            return response.getBody(AuthorizationResponse.class);

        } catch (HttpResponseException hre) {
            errorHandler.handleError(
                    hre, BankverlagErrorHandler.ErrorSource.AUTHORISATION_USERNAME_PASSWORD);
            throw hre;
        }
    }

    public AuthorizationResponse selectAuthorizationMethod(String url, String methodId) {
        try {
            return requestBuilder
                    .createRequest(new URL(url))
                    .put(
                            AuthorizationResponse.class,
                            new SelectAuthorizationMethodRequest(methodId));
        } catch (HttpResponseException httpResponseException) {
            errorHandler.handleError(
                    httpResponseException, ErrorSource.SELECT_AUTHORIZATION_METHOD);
            throw httpResponseException;
        }
    }

    public AuthorizationResponse getAuthorizationStatus(String url) {
        try {
            return requestBuilder.createRequest(new URL(url)).get(AuthorizationResponse.class);
        } catch (HttpResponseException httpResponseException) {
            errorHandler.handleError(httpResponseException, ErrorSource.GET_AUTHORIZATION_STATUS);
            throw httpResponseException;
        }
    }

    public AuthorizationResponse finalizeAuthorization(String url, String otp) {
        try {
            return requestBuilder
                    .createRequest(new URL(url))
                    .put(AuthorizationResponse.class, new FinalizeAuthorizationRequest(otp));
        } catch (HttpResponseException hre) {
            errorHandler.handleError(hre, BankverlagErrorHandler.ErrorSource.OTP_STEP);
            throw hre;
        }
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return requestBuilder
                .createRequest(Urls.CONSENT_DETAILS.parameter(PathVariables.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    public FetchAccountsResponse fetchAccounts(String consentId) {
        return createRequestInSession(Urls.FETCH_ACCOUNTS, consentId)
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
                                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED),
                        consentId)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    public HttpResponse getTransactionsZipFile(
            String consentId, String accountId, LocalDate startDate) {

        FetchTransactionsResponse fetchTransactionsResponse =
                createRequestInSession(
                                Urls.FETCH_TRANSACTIONS
                                        .parameter(PathVariables.ACCOUNT_ID, accountId)
                                        .queryParam(QueryKeys.DATE_FROM, startDate.toString())
                                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED),
                                consentId)
                        .get(FetchTransactionsResponse.class);

        return getTransactionsFile(
                consentId, new URL(fetchTransactionsResponse.getLinks().getDownload().getHref()));
    }

    private HttpResponse getTransactionsFile(String consentId, URL file) {
        return createRequestInSessionWithTypes(
                        file,
                        consentId,
                        MediaType.APPLICATION_OCTET_STREAM_TYPE,
                        MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .get(HttpResponse.class);
    }
}
