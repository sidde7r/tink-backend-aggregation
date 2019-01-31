package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.AuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc.BillingUnitsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc.ReservationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebKortApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final SebKortConfiguration config;

    public SebKortApiClient(
            TinkHttpClient client, SessionStorage sessionStorage, SebKortConfiguration config) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createFormRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(
                        SebKortConstants.StorageKey.AUTHORIZATION,
                        sessionStorage.get(SebKortConstants.StorageKey.AUTHORIZATION))
                .queryParam(
                        SebKortConstants.QueryKey.LANGUAGE_CODE,
                        SebKortConstants.QueryValue.LANGUAGE_CODE);
    }

    public BankIdCollectResponse collectBankId(URL collectUrl, BankIdCollectRequest request) {
        return createRequest(collectUrl).post(BankIdCollectResponse.class, request);
    }

    public BankIdCompleteResponse completeBankId(URL completeUrl) {
        return createRequest(completeUrl).get(BankIdCompleteResponse.class);
    }

    public BankIdInitResponse initBankId(BankIdInitRequest request) {
        return createRequest(SebKortConstants.Urls.BANK_ID_INIT)
                .queryParam(SebKortConstants.QueryKey.METHOD, config.getBankIdMethod())
                .queryParam(SebKortConstants.QueryKey.PROFILE, SebKortConstants.QueryValue.PROFILE)
                .queryParam(
                        SebKortConstants.QueryKey.LANGUAGE, SebKortConstants.QueryValue.LANGUAGE)
                .queryParam(SebKortConstants.QueryKey.PREFILLED_SUBJECT, request.getSubject())
                .queryParam(SebKortConstants.QueryKey.TARGET, SebKortConstants.QueryValue.TARGET)
                .post(BankIdInitResponse.class, request);
    }

    public LoginResponse login(LoginRequest request) {
        return createFormRequest(SebKortConstants.Urls.SEBKORT_LOGIN)
                .post(LoginResponse.class, request);
    }

    public void logout() {
        createRequestInSession(SebKortConstants.Urls.SEBKORT_LOGOUT)
                .queryParam(
                        SebKortConstants.QueryKey.REDIRECT,
                        String.format(
                                SebKortConstants.QueryValue.REDIRECT, config.getProviderCode()))
                .get(HttpResponse.class);
    }

    public AuthResponse auth(AuthRequest request) {
        client.getInternalClient().setFollowRedirects(true);

        return createFormRequest(SebKortConstants.Urls.SEBKORT_AUTH)
                .post(AuthResponse.class, request);
    }

    public BillingUnitsResponse fetchBillingUnits() {
        return createRequest(SebKortConstants.Urls.BILLING_UNITS)
                .get(BillingUnitsResponse.class);
    }

    public TransactionsResponse fetchTransactions(
            String cardAccountId, Date fromDate, Date toDate) {
        return createRequestInSession(SebKortConstants.Urls.SEBKORT_TRANSACTIONS)
                .queryParam(SebKortConstants.QueryKey.CARD_ACCOUNT_ID, cardAccountId)
                .queryParam(
                        SebKortConstants.QueryKey.FROM_DATE,
                        SebKortConstants.DATE_FORMAT.format(fromDate))
                .queryParam(
                        SebKortConstants.QueryKey.TO_DATE,
                        SebKortConstants.DATE_FORMAT.format(toDate))
                .get(TransactionsResponse.class);
    }

    public ReservationsResponse fetchReservations(
            String cardAccountId, Date fromDate, Date toDate) {
        return createRequestInSession(SebKortConstants.Urls.SEBKORT_RESERVATIONS)
                .queryParam(SebKortConstants.QueryKey.CARD_ACCOUNT_ID, cardAccountId)
                .queryParam(
                        SebKortConstants.QueryKey.FROM_DATE,
                        SebKortConstants.DATE_FORMAT.format(fromDate))
                .queryParam(
                        SebKortConstants.QueryKey.TO_DATE,
                        SebKortConstants.DATE_FORMAT.format(toDate))
                .get(ReservationsResponse.class);
    }
}
