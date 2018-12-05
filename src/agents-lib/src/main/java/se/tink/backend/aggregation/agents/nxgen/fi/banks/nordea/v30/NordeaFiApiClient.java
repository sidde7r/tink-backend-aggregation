package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.entities.Form;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.rpc.FetchCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.rpc.FetchInvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaFiApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final String username;

    public NordeaFiApiClient(TinkHttpClient client, SessionStorage sessionStorage, String username) {

        this.client = client;
        this.sessionStorage = sessionStorage;
        this.username = username;
    }

    public AuthenticateResponse initCodesAuthentication() throws HttpResponseException {
        Form formBuilder = defaultAuthenticationForm(username);

        return sendAuthenticateRequest(formBuilder);
    }

    public AuthenticateResponse pollCodesAuthentication(String reference) throws HttpResponseException {

        Form formBuilder = defaultAuthenticationForm(username);
        formBuilder.put(NordeaFiConstants.FormParams.AUTH_REF, reference);
        return sendAuthenticateRequest(formBuilder);
    }

    public FetchAccountResponse fetchAccounts() {

        return createRequest(NordeaFiConstants.Urls.FETCH_PRODUCT, getAccessToken())
                .queryParam(NordeaFiConstants.QueryParams.PRODUCT_CATEGORY, NordeaFiConstants.Products.ACCOUNT)
                .get(FetchAccountResponse.class);

    }

    public <T> T fetchTransactions(int offset, int limit, String accountId, String product, Class<T> responseType) {

        return createRequest(NordeaFiConstants.Urls.FETCH_TRANSACTIONS, getAccessToken())
                .queryParam(NordeaFiConstants.QueryParams.OFFSET, Integer.toString(offset))
                .queryParam(NordeaFiConstants.QueryParams.LIMIT, Integer.toString(limit))
                .queryParam(NordeaFiConstants.QueryParams.PRODUCT_ID, accountId)
                .queryParam(NordeaFiConstants.QueryParams.PRODUCT_CATEGORY, product)
                .get(responseType);
    }

    public FetchCardsResponse fetchCards() {

        return createRequest(NordeaFiConstants.Urls.FETCH_CARDS_DETAILED, getAccessToken())
                .get(FetchCardsResponse.class);
    }

    public HttpResponse fetchLoans() {
        return createRequest(NordeaFiConstants.Urls.FETCH_PRODUCT, getAccessToken())
                .queryParam(NordeaFiConstants.QueryParams.PRODUCT_CATEGORY, NordeaFiConstants.Products.LOAN)
                .get(HttpResponse.class);
    }

    public FetchInvestmentsResponse fetchInvestments() {

        return createRequest(NordeaFiConstants.Urls.FETCH_SAVINGS, getAccessToken())
                .queryParam(NordeaFiConstants.QueryParams.TYPE, NordeaFiConstants.Products.SAVINGS)
                .get(FetchInvestmentsResponse.class);
    }

    public AuthenticateResponse keepAlive() throws SessionException {
        try {
            String refreshToken = getRefreshToken();

            if (refreshToken != null) {

                Form formBuilder = defaultAuthenticationForm(username);
                formBuilder.put(NordeaFiConstants.FormParams.GRANT_TYPE, NordeaFiConstants.SessionStorage.REFRESH_TOKEN);
                formBuilder.put(NordeaFiConstants.SessionStorage.REFRESH_TOKEN, refreshToken);

                 return sendAuthenticateRequest(formBuilder);
            } else {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            throw e;
        }
    }

    public void logout() {

        Form form = new Form();
        form.put(NordeaFiConstants.FormParams.TOKEN, getAccessToken());
        form.put(NordeaFiConstants.FormParams.TOKEN_TYPE_HINT, "access_token");

        createRequest(NordeaFiConstants.Urls.LOGOUT)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post();
    }

    private AuthenticateResponse sendAuthenticateRequest(AbstractForm form) throws HttpResponseException {

        return createRequest(NordeaFiConstants.Urls.AUTHENTICATE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(AuthenticateResponse.class);
    }

    private Form defaultAuthenticationForm(String username) {
        Form formBuilder;
        formBuilder = new Form(NordeaFiConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(NordeaFiConstants.FormParams.USERNAME, username);

        return formBuilder;
    }

    private RequestBuilder createRequest(URL url, String accessToken) {
        return createRequest(url)
                .header(HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + accessToken);
    }

    private RequestBuilder createRequest(URL url) {

        return client
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private String getTokenType() {
        return sessionStorage.get(NordeaFiConstants.SessionStorage.TOKEN_TYPE);
    }

    private String getAccessToken() {
        return sessionStorage.get(NordeaFiConstants.SessionStorage.ACCESS_TOKEN);
    }

    private String getRefreshToken() {
        return sessionStorage.get(NordeaFiConstants.SessionStorage.REFRESH_TOKEN);
    }
}
