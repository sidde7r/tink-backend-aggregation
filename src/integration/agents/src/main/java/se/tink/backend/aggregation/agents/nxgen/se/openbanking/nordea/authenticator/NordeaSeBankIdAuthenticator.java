package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.GetCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSeBankIdAuthenticator implements BankIdAuthenticator {
    private final NordeaSeApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaSeBankIdAuthenticator(NordeaSeApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Object init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        sessionStorage.put(NordeaSeConstants.StorageKeys.SSN, ssn);
        AuthorizeRequest authorizeRequest = getAuthorizeRequest();

        AuthorizeResponse authorizeResponse = apiClient.authorize(authorizeRequest);

        saveTppToken(authorizeResponse);
        saveOrderRef(authorizeResponse);

        return null;
    }

    @Override
    public BankIdStatus collect(Object reference)
            throws AuthenticationException, AuthorizationException {

        try {
            HttpResponse response = apiClient.getCode();
            if (response.getStatus() == 304) {
                return BankIdStatus.WAITING;
            } else {
                return handleBankIdDone(response);
            }
        } catch (HttpResponseException e) {
            if (e.getResponse()
                    .getBody(String.class)
                    .contains("nsp.returncode.cava.user_cancel_error")) {
                return BankIdStatus.CANCELLED;
            }
        }
        throw new IllegalStateException();
    }

    private BankIdStatus handleBankIdDone(HttpResponse response) {
        GetCodeResponse getCodeResponse = response.getBody(GetCodeResponse.class);

        GetTokenForm form = getGetTokenForm(getCodeResponse);

        OAuth2Token accessToken = apiClient.getToken(form);
        apiClient.setTokenToSession(accessToken);

        return BankIdStatus.DONE;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    private GetTokenForm getGetTokenForm(GetCodeResponse getCodeResponse) {
        return GetTokenForm.builder()
                .setCode(getCodeResponse.getResponse().getCode())
                .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                .setRedirectUri(apiClient.getConfiguration().getRedirectUrl())
                .build();
    }

    private AuthorizeRequest getAuthorizeRequest() {
        return new AuthorizeRequest(
                NordeaSeConstants.FormValues.DURATION,
                sessionStorage.get(NordeaSeConstants.StorageKeys.SSN),
                apiClient.getConfiguration().getRedirectUrl(),
                NordeaSeConstants.FormValues.RESPONSE_TYPE,
                Arrays.asList(
                        NordeaSeConstants.FormValues.ACCOUNTS_BALANCES,
                        NordeaSeConstants.FormValues.ACCOUNTS_BASIC,
                        NordeaSeConstants.FormValues.ACCOUNTS_DETAILS,
                        NordeaSeConstants.FormValues.ACCOUNTS_TRANSACTIONS,
                        NordeaSeConstants.FormValues.PAYMENTS_MULTIPLE),
                NordeaSeConstants.FormValues.STATE);
    }

    private void saveTppToken(AuthorizeResponse authorizeResponse) {
        sessionStorage.put(
                NordeaSeConstants.StorageKeys.TPP_TOKEN,
                authorizeResponse.getResponse().getTppToken());
    }

    private void saveOrderRef(AuthorizeResponse authorizeResponse) {
        sessionStorage.put(
                NordeaSeConstants.StorageKeys.ORDER_REF,
                authorizeResponse.getResponse().getOrderRef());
    }
}
