package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.TransactionalDetailsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IberCajaApiClient {

    private final TinkHttpClient httpClient;

    public IberCajaApiClient(TinkHttpClient httpClient) {

        this.httpClient = httpClient;
    }

    public SessionResponse initializeSession(SessionRequest sessionRequest) throws LoginException {

        String response = createRequest(IberCajaConstants.Urls.INIT_LOGIN)
                .post(String.class, sessionRequest);

        SessionResponse sessionResponse = SerializationUtils.deserializeFromString(response, SessionResponse.class);

        if (sessionResponse.getTicket() == null) {
            ErrorResponse errorResponse = SerializationUtils.deserializeFromString(response, ErrorResponse.class);
            errorResponse.logError();
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else {
            return sessionResponse;
        }
    }

    public LoginResponse login(LoginRequest loginRequest, String ticket, String user) {

        return createRequest(IberCajaConstants.Urls.LOGIN)
                .header(IberCajaConstants.Headers.USER, user)
                .header(IberCajaConstants.Headers.TICKET, ticket)
                .post(LoginResponse.class, loginRequest);
    }

    public FetchAccountResponse fetchAccountList(String ticket, String user) {

        return createRequest(IberCajaConstants.Urls.FETCH_MAIN_ACCOUNT)
                .header(IberCajaConstants.Headers.USER, user)
                .header(IberCajaConstants.Headers.TICKET, ticket)
                .get(FetchAccountResponse.class);
    }

    public TransactionalDetailsResponse fetchTransactionDetails(String bankIdentifier, String ticket, String dateMin,
            String dateMax, String user) {

        return createRequest(IberCajaConstants.Urls.FETCH_ACCOUNT_TRANSACTION)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_ACCOUNT, bankIdentifier)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_START_DATE, dateMin)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_END_DATE, dateMax)
                .header(IberCajaConstants.Headers.USER, user)
                .header(IberCajaConstants.Headers.TICKET, ticket)
                .get(TransactionalDetailsResponse.class);
    }

    public FetchAccountResponse fetchInvestmentAccounList(String ticket, String user) {

        return createRequest(IberCajaConstants.Urls.FETCH_MAIN_ACCOUNT)
                .header(IberCajaConstants.Headers.USER, user)
                .header(IberCajaConstants.Headers.TICKET, ticket)
                .get(FetchAccountResponse.class);
    }

    public String fetchInvestmentTransactionDetails(String bankIdentifier, String ticket, String user) {
        return createRequest(IberCajaConstants.Urls.FETCH_INVESTMENT_ACCOUNT_TRANSACTION)
                .queryParam(IberCajaConstants.QueryParams.ACCOUNT, bankIdentifier)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_IS_SPECIALIIST,
                        IberCajaConstants.DefaultRequestParams.IS_SPECIALIST)
                .header(IberCajaConstants.Headers.USER, user)
                .header(IberCajaConstants.Headers.TICKET, ticket)
                .get(String.class);
    }

    public FetchAccountResponse fetchCreditCardsList(String ticket, String user) {

        return createRequest(IberCajaConstants.Urls.FETCH_MAIN_ACCOUNT)
                .header(IberCajaConstants.Headers.USER, user)
                .header(IberCajaConstants.Headers.TICKET, ticket)
                .get(FetchAccountResponse.class);
    }

    public CreditCardResponse fetchCreditCardsTransactionList(String bankIdentifier, String requestOrden,
            String requestTipo, String ticket, String user, String dateMin,
            String dateMax) {

        return createRequest(IberCajaConstants.Urls.FETCH_CREDIT_CARD_ACCOUNT)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_CARD, bankIdentifier)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_ORDER, requestOrden)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_CARD_TYPE, requestTipo)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_START_DATE, dateMin)
                .queryParam(IberCajaConstants.QueryParams.REQUEST_END_DATE, dateMax)
                .header(IberCajaConstants.Headers.USER, user)
                .header(IberCajaConstants.Headers.TICKET, ticket)
                .get(CreditCardResponse.class);
    }

    public boolean isAlive() {

        try {

            createRequest(IberCajaConstants.Urls.KEEP_ALIVE).get(HttpResponse.class);
        } catch (HttpResponseException e) {

            return false;
        }

        return true;
    }

    private RequestBuilder createRequest(URL url) {

        return httpClient
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(IberCajaConstants.Headers.PLAYBACK_MODE,
                        IberCajaConstants.DefaultRequestParams.PLAYBACK_MODE_REAL);
    }

}
