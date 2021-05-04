package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.BankIdPollRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
@RequiredArgsConstructor
public class NordnetBankIdAutoStartAuthenticator implements BankIdAuthenticator<String> {

    private final NordnetBaseApiClient apiClient;
    private final SessionStorage sessionStorage;

    private String autoStartToken;
    private String ssn;

    @Override
    public String init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        this.ssn = ssn;
        return refreshAutostartToken();
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        final PollBankIdResponse response;

        try {
            final BankIdPollRequest request = new BankIdPollRequest(reference);
            final RequestBuilder requestBuilder =
                    apiClient
                            .createRequestInSession(new URL(Urls.BANKID_POLL))
                            .type(MediaType.APPLICATION_JSON)
                            .overrideHeader(HttpHeaders.USER_AGENT, HeaderValues.REACT_NATIVE_AGENT)
                            .body(request);
            response = apiClient.post(requestBuilder, PollBankIdResponse.class);
        } catch (HttpResponseException e) {
            return handleBankIdPollErrors(e);
        }

        if (response.isLoggedIn()) {
            setSessionKey(response.getSessionKey());
            checkIdentity();

            return BankIdStatus.DONE;
        }
        return response.getBankIdStatus();
    }

    private void setSessionKey(String sessionKey) {
        sessionStorage.put(StorageKeys.SESSION_KEY, sessionKey + ":" + sessionKey);
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public String refreshAutostartToken()
            throws BankServiceException, AuthorizationException, AuthenticationException {
        initLogin();
        final InitBankIdResponse response = initBankId();
        this.autoStartToken = response.getAutoStartToken();
        Preconditions.checkNotNull(
                Strings.emptyToNull(this.autoStartToken), "Autostart token must be present");

        return response.getOrderRef();
    }

    private InitBankIdResponse initBankId() {
        RequestBuilder requestBuilder =
                apiClient
                        .createRequestInSession(new URL(Urls.BANKID_START))
                        .type(MediaType.APPLICATION_JSON)
                        .overrideHeader(HttpHeaders.USER_AGENT, HeaderValues.NORDNET_AGENT)
                        .body(new InitBankIdRequest(HeaderValues.REACT_NATIVE_AGENT));

        try {
            return apiClient.post(requestBuilder, InitBankIdResponse.class);
        } catch (HttpResponseException e) {
            handleBankIdErrors(e);
            throw BankIdError.UNKNOWN.exception(e);
        }
    }

    private void initLogin() {
        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.INIT_LOGIN))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .overrideHeader(HttpHeaders.USER_AGENT, HeaderValues.REACT_NATIVE_AGENT)
                        .body(FormValues.ANONYMOUS_LOGIN.serialize());

        final LoginResponse response = apiClient.post(requestBuilder, LoginResponse.class);

        setSessionKey(response.getSessionKey());
    }

    private void checkIdentity() throws LoginException {
        final FetchIdentityDataResponse identityData = apiClient.fetchIdentityData();
        if (!ssn.equalsIgnoreCase(identityData.getIdentityData().getSsn())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private void handleBankIdErrors(HttpResponseException e)
            throws BankIdException, LoginException {
        if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
            throw LoginError.NOT_CUSTOMER.exception();
        } else if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception(e);
        } else if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
        }
    }

    private BankIdStatus handleBankIdPollErrors(HttpResponseException e) {
        if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
            final PollBankIdResponse response = e.getResponse().getBody(PollBankIdResponse.class);
            final String hintCode = response.getHintCode();
            if (NordnetBaseConstants.BankIdStatus.USER_CANCEL.equalsIgnoreCase(hintCode)) {
                return BankIdStatus.CANCELLED;
            } else if (NordnetBaseConstants.BankIdStatus.START_FAILED.equalsIgnoreCase(hintCode)) {
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            }
        }
        handleBankIdErrors(e);
        throw BankIdError.UNKNOWN.exception();
    }
}
