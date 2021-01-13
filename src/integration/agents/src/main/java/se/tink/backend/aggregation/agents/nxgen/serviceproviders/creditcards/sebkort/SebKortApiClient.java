package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.AuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.exceptions.SebKortUnexpectedMediaTypeException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc.ReservationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebKortApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final SebKortConfiguration config;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");

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
                .header(
                        SebKortConstants.Headers.USER_AGENT,
                        SebKortConstants.Headers.USER_AGENT_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(
                        SebKortConstants.Headers.USER_AGENT,
                        SebKortConstants.Headers.USER_AGENT_VALUE)
                .header(
                        SebKortConstants.StorageKey.AUTHORIZATION,
                        sessionStorage.get(SebKortConstants.StorageKey.AUTHORIZATION))
                .queryParam(
                        SebKortConstants.QueryKey.LANGUAGE_CODE,
                        SebKortConstants.QueryValue.LANGUAGE_CODE);
    }

    private RequestBuilder fetchTransactionsFor(Date fromDate, Date toDate) {
        return createRequestInSession(SebKortConstants.Urls.SEBKORT_TRANSACTIONS)
                .queryParam(SebKortConstants.QueryKey.FROM_DATE, dateFormat.format(fromDate))
                .queryParam(SebKortConstants.QueryKey.TO_DATE, dateFormat.format(toDate));
    }

    public TransactionsResponse fetchTransactionsForContractId(
            String cardContractId, Date fromDate, Date toDate) {
        return fetchTransactionsFor(fromDate, toDate)
                .queryParam(SebKortConstants.QueryKey.CARD_CONTRACT_ID, cardContractId)
                .get(TransactionsResponse.class);
    }

    public ReservationsResponse fetchReservationsForContractId(String cardContractId) {
        return createRequestInSession(SebKortConstants.Urls.SEBKORT_RESERVATIONS)
                .queryParam(SebKortConstants.QueryKey.CARD_CONTRACT_ID, cardContractId)
                .get(ReservationsResponse.class);
    }

    public TransactionsResponse fetchTransactionsForCardAccountId(
            String cardAccountId, Date fromDate, Date toDate) {
        return fetchTransactionsFor(fromDate, toDate)
                .queryParam(SebKortConstants.QueryKey.CARD_ACCOUNT_ID, cardAccountId)
                .get(TransactionsResponse.class);
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
                .header(
                        SebKortConstants.Headers.REFERER,
                        "https://secure.sebkort.com/nis/m/sase/external/t/login/index")
                .post(AuthResponse.class, request);
    }

    public CardsResponse fetchCards() {
        HttpResponse response =
                createRequestInSession(SebKortConstants.Urls.SEBKORT_CARDS)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
                        .get(HttpResponse.class);

        if (response.getType().getType().equals(MediaType.APPLICATION_JSON_TYPE.getType())
                && response.getType()
                        .getSubtype()
                        .equals(MediaType.APPLICATION_JSON_TYPE.getSubtype())) {
            return response.getBody(CardsResponse.class);
        } else {
            throw new SebKortUnexpectedMediaTypeException(response.getBody(String.class));
        }
    }
}
