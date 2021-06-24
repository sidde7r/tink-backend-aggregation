package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.TransactionalDetailsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceDownExceptionFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IberCajaApiClient {

    private final TinkHttpClient httpClient;
    private final IberCajaSessionStorage iberCajaSessionStorage;

    public IberCajaApiClient(
            TinkHttpClient httpClient, IberCajaSessionStorage iberCajaSessionStorage) {
        this.httpClient = httpClient;
        this.iberCajaSessionStorage = iberCajaSessionStorage;
        this.httpClient.addFilter(new TimeoutRetryFilter(3, 1000, HttpClientException.class));
        this.httpClient.addFilter(new TimeoutFilter());
        this.httpClient.addFilter(new BankServiceDownExceptionFilter());
    }

    public SessionResponse initializeSession(SessionRequest sessionRequest) throws LoginException {

        String response =
                createRequest(IberCajaConstants.Urls.INIT_LOGIN).post(String.class, sessionRequest);

        SessionResponse sessionResponse =
                SerializationUtils.deserializeFromString(response, SessionResponse.class);

        if (sessionResponse.getTicket() == null) {
            ErrorResponse errorResponse =
                    SerializationUtils.deserializeFromString(response, ErrorResponse.class);
            errorResponse.logError();
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else {
            return sessionResponse;
        }
    }

    public FetchAccountResponse fetchAccountList() {

        return createRequestInSession(IberCajaConstants.Urls.FETCH_MAIN_ACCOUNT)
                .get(FetchAccountResponse.class);
    }

    public TransactionalDetailsResponse fetchTransactionDetails(
            String bankIdentifier, String dateMin, String dateMax) {

        return createRequestInSession(IberCajaConstants.Urls.FETCH_ACCOUNT_TRANSACTION)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_ACCOUNT, bankIdentifier)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_START_DATE, dateMin)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_END_DATE, dateMax)
                .get(TransactionalDetailsResponse.class);
    }

    public FetchAccountResponse fetchInvestmentAccounList() {

        return createRequestInSession(IberCajaConstants.Urls.FETCH_MAIN_ACCOUNT)
                .get(FetchAccountResponse.class);
    }

    public String fetchInvestmentTransactionDetails(String bankIdentifier) {
        return createRequestInSession(IberCajaConstants.Urls.FETCH_INVESTMENT_ACCOUNT_TRANSACTION)
                .queryParam(IberCajaConstants.QueryParams.ACCOUNT, bankIdentifier)
                .queryParam(
                        IberCajaConstants.QueryParams.REQUEST_IS_SPECIALIIST,
                        IberCajaConstants.DefaultRequestParams.IS_SPECIALIST)
                .get(String.class);
    }

    public FetchAccountResponse fetchCreditCardsList() {

        return createRequestInSession(IberCajaConstants.Urls.FETCH_MAIN_ACCOUNT)
                .get(FetchAccountResponse.class);
    }

    public CreditCardResponse fetchCreditCardsTransactionList(
            String bankIdentifier,
            String requestOrden,
            String requestTipo,
            String dateMin,
            String dateMax) {

        return createRequestInSession(IberCajaConstants.Urls.FETCH_CREDIT_CARD_ACCOUNT)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_CARD, bankIdentifier)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_ORDER, requestOrden)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_CARD_TYPE, requestTipo)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_START_DATE, dateMin)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_END_DATE, dateMax)
                .get(CreditCardResponse.class);
    }

    public boolean isAlive() {
        try {
            HttpResponse httpResponse =
                    createRequestInSession(Urls.KEEP_ALIVE).get(HttpResponse.class);
            String contentType =
                    httpResponse.getHeaders().getFirst(IberCajaConstants.Headers.CONTENT_TYPE);
            if (contentType.startsWith(MediaType.TEXT_HTML)) {
                return false;
            } else {
                ErrorResponse response = httpResponse.getBody(ErrorResponse.class);
                return response.getNumber() == 0;
            }
        } catch (HttpResponseException | HttpClientException e) {
            return false;
        }
    }

    private RequestBuilder createRequest(URL url) {

        return httpClient
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        IberCajaConstants.Headers.PLAYBACK_MODE,
                        IberCajaConstants.DefaultRequestParams.PLAYBACK_MODE_REAL)
                .header(
                        IberCajaConstants.Headers.APP_ID,
                        IberCajaConstants.DefaultRequestParams.APP_ID)
                .header(
                        IberCajaConstants.Headers.VERSION,
                        IberCajaConstants.DefaultRequestParams.VERSION)
                .header(
                        IberCajaConstants.Headers.CHANNEL,
                        IberCajaConstants.DefaultRequestParams.CHANNEL_MOBILE)
                .header(
                        IberCajaConstants.Headers.DEVICE,
                        IberCajaConstants.DefaultRequestParams.DEVICE)
                .header(
                        IberCajaConstants.Headers.ACCESS_CARD,
                        IberCajaConstants.DefaultRequestParams.ACCESS_CARD);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(IberCajaConstants.Headers.USER, iberCajaSessionStorage.getUsername())
                .header(IberCajaConstants.Headers.TICKET, iberCajaSessionStorage.getTicket())
                .header(IberCajaConstants.Headers.NICI, iberCajaSessionStorage.getNici())
                .header(IberCajaConstants.Headers.NIP, iberCajaSessionStorage.getNip())
                .header(
                        IberCajaConstants.Headers.TOKEN_IDENTITY,
                        iberCajaSessionStorage.getTokenIdentity())
                .header(
                        IberCajaConstants.Headers.CONTRACT,
                        iberCajaSessionStorage.getContractInCourse());
    }
}
