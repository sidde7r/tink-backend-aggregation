package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator;

import com.google.common.base.Preconditions;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.InitLogin;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.Patterns;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.BankIdPollRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class NordnetBankIdAutoStartAuthenticator implements BankIdAuthenticator<String> {

    private final NordnetBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private String autoStartToken;
    private String ssn;
    private String ntag;

    public NordnetBankIdAutoStartAuthenticator(
            NordnetBaseApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        this.ssn = ssn;

        initLogin();
        anonymousLogin();

        final InitBankIdResponse response = initBankId();
        this.autoStartToken = response.getAutostartToken();

        return response.getOrderRef();
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {

        final HttpResponse response = pollBankId(reference);

        if (response.getBody(String.class).contains(InitLogin.AUTHENTICATED)) {
            getNtag(response);
            fetchOauth2Token(getCode());

            return BankIdStatus.DONE;
        }
        return response.getBody(PollBankIdResponse.class).getBankIdStatus();
    }

    private HttpResponse pollBankId(String reference) {
        final BankIdPollRequest request = new BankIdPollRequest(reference);

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.BANKID_POLL))
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.NTAG, ntag)
                        .body(request);

        try {
            return apiClient.post(requestBuilder, HttpResponse.class);
        } catch (HttpResponseException e) {
            handleBankIdErrors(e);
            throw e;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    private void anonymousLogin() {
        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.ANONYMOUS_LOGIN))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.NTAG, HeaderValues.NO_NTAG_RECEIVED_YET)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .header(HeaderKeys.REFERER, HeaderValues.REFERER)
                        .acceptLanguage(Locale.UK);

        HttpResponse response = apiClient.post(requestBuilder, HttpResponse.class);
        getNtag(response);
    }

    private InitBankIdResponse initBankId() {
        String requestBody = Form.builder().put(FormKeys.SSN, ssn).build().serialize();

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.BANKID_START))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .header(HeaderKeys.NTAG, ntag)
                        .header(HeaderKeys.REFERER, HeaderValues.REFERER)
                        .body(requestBody);

        try {
            return apiClient.post(requestBuilder, InitBankIdResponse.class);
        } catch (HttpResponseException e) {
            handleBankIdErrors(e);
            throw e;
        }
    }

    private void initLogin() {
        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.INIT_LOGIN))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .body(FormValues.ANONYMOUS_LOGIN.serialize());

        apiClient.post(requestBuilder, LoginResponse.class);

        oauth2Authorize();
    }

    private void oauth2Authorize() {

        String location;

        RequestBuilder authRequest =
                apiClient
                        .createBasicRequest(
                                new URL(Urls.INIT_AUTHORIZE)
                                        .queryParam(QueryKeys.AUTH_TYPE, QueryValues.AUTH_TYPE)
                                        .queryParam(FormKeys.CLIENT_ID, QueryValues.CLIENT_ID)
                                        .queryParam(
                                                NordnetBaseConstants.FormKeys.RESPONSE_TYPE,
                                                QueryValues.RESPONSE_TYPE)
                                        .queryParam(
                                                NordnetBaseConstants.FormKeys.REDIRECT_URI,
                                                QueryValues.REDIRECT_URI))
                        .accept(
                                MediaType.TEXT_PLAIN,
                                MediaType.APPLICATION_XHTML_XML,
                                HeaderKeys.APPLICATION_XML_Q,
                                HeaderKeys.GENERIC_MEDIA_TYPE)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT);

        HttpResponse authResponse = apiClient.get(authRequest, HttpResponse.class);

        location = getLocation(authResponse);
        RequestBuilder auth2Request =
                apiClient
                        .createBasicRequest(new URL(location))
                        .accept(
                                MediaType.TEXT_PLAIN,
                                MediaType.APPLICATION_XHTML_XML,
                                HeaderKeys.APPLICATION_XML_Q,
                                HeaderKeys.GENERIC_MEDIA_TYPE)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT);

        HttpResponse auth2Response = apiClient.get(auth2Request, HttpResponse.class);

        location = getLocation(auth2Response);
        RequestBuilder auth3Request =
                apiClient
                        .createBasicRequest(new URL(Urls.AUTH_BASE + location))
                        .accept(
                                MediaType.TEXT_PLAIN,
                                MediaType.APPLICATION_XHTML_XML,
                                HeaderKeys.APPLICATION_XML_Q,
                                HeaderKeys.GENERIC_MEDIA_TYPE)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT);

        apiClient.get(auth3Request, HttpResponse.class);
    }

    private void fetchOauth2Token(final String code) {

        final String requestBody =
                Form.builder()
                        .put(QueryValues.RESPONSE_TYPE, code)
                        .put(FormKeys.CLIENT_ID, QueryValues.CLIENT_ID)
                        .put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET)
                        .put(FormKeys.GRANT_TYPE, FormValues.GRANT_TYPE)
                        .put(FormKeys.REDIRECT_URI, QueryValues.REDIRECT_URI)
                        .build()
                        .serialize();

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.OAUTH2_TOKEN))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.NORDNET_AGENT)
                        .body(requestBody);

        HttpResponse response = apiClient.post(requestBuilder, HttpResponse.class);

        final String forwardUri = getLocation(response);
        requestBuilder =
                apiClient
                        .createBasicRequest(new URL(forwardUri))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.NORDNET_AGENT)
                        .body(requestBody);

        TokenResponse token = apiClient.post(requestBuilder, TokenResponse.class);
        persistentStorage.put(StorageKeys.OAUTH2_TOKEN, token);
    }

    private String getCode() {

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(
                                new URL(Urls.OAUTH2_AUTHORIZE)
                                        .queryParam(
                                                NordnetBaseConstants.FormKeys.CLIENT_ID,
                                                QueryValues.CLIENT_ID)
                                        .queryParam(
                                                NordnetBaseConstants.FormKeys.RESPONSE_TYPE,
                                                QueryValues.RESPONSE_TYPE)
                                        .queryParam(
                                                NordnetBaseConstants.FormKeys.REDIRECT_URI,
                                                QueryValues.REDIRECT_URI)
                                        .queryParam("b", "1"))
                        .accept(
                                MediaType.TEXT_PLAIN,
                                MediaType.APPLICATION_XHTML_XML,
                                HeaderKeys.APPLICATION_XML_Q,
                                HeaderKeys.GENERIC_MEDIA_TYPE)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.USER_AGENT)
                        .header(HeaderKeys.REFERER, HeaderValues.REFERER);

        HttpResponse response = apiClient.get(requestBuilder, HttpResponse.class);
        return getAuthCodeFrom(
                response.getHeaders().get(HeaderKeys.LOCATION).stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException("Fetch authorization code error")));
    }

    private String getAuthCodeFrom(String location) {
        Matcher matcher = Patterns.CODE.matcher(location);

        return matcher.find() ? matcher.group(1) : null;
    }

    private void getNtag(HttpResponse response) {
        this.ntag = response.getHeaders().getFirst(HeaderKeys.NTAG);
        Preconditions.checkNotNull(ntag, "Expected ntag header to exist for subsequent requests");
    }

    private String getLocation(HttpResponse response) {
        final String location = response.getHeaders().getFirst(HeaderKeys.LOCATION);
        Preconditions.checkNotNull(
                location, "Expected Location header to exist for subsequent requests");
        return location;
    }

    private BankIdStatus handleBankIdErrors(HttpResponseException e)
            throws BankIdException, LoginException {
        if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
            throw LoginError.NOT_CUSTOMER.exception();
        } else if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception(e);
        } else if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
        } else if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
            PollBankIdResponse response = e.getResponse().getBody(PollBankIdResponse.class);
            if (NordnetBaseConstants.BankIdStatus.USER_CANCEL.equalsIgnoreCase(
                    response.getHintCode())) {
                throw BankIdError.CANCELLED.exception(e);
            }
        }
        throw BankIdError.UNKNOWN.exception();
    }
}
