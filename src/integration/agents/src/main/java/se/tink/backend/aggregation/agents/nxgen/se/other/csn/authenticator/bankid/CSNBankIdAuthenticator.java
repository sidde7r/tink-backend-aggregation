package se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class CSNBankIdAuthenticator implements BankIdAuthenticator<String> {
    private final CSNApiClient apiClient;
    private final SessionStorage sessionStorage;

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        final HttpResponse loginResponse = apiClient.initBankId(new LoginForm(ssn));
        if (loginResponse.getBody(String.class).contains("Fel på sidan")) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        sessionStorage.put(
                CSNConstants.Storage.SESSION_ID, apiClient.extractSessionId(loginResponse));
        return sessionStorage.get(CSNConstants.Storage.SESSION_ID);
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        final HttpResponse pollBankIdResponse = apiClient.pollBankId();

        switch (pollBankIdResponse.getBody(String.class)) {
            case CSNConstants.BankIdStatus.COLLECT:
            case CSNConstants.BankIdStatus.RECEIVED:
                return BankIdStatus.WAITING;
            case CSNConstants.BankIdStatus.CONTINUE:
                apiClient.initBankId(new LoginForm());
                sessionStorage.put(
                        CSNConstants.Storage.ACCESS_TOKEN, apiClient.extractAccessToken());
                return BankIdStatus.DONE;
        }

        return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    public static class LoginForm extends MultivaluedMapImpl {
        LoginForm(String username) {
            this.add(CSNConstants.Login.METHOD, CSNConstants.Login.VALIDATE_BANK_ID);
            this.add(CSNConstants.Login.SSN, username);
        }

        LoginForm() {
            this.add(CSNConstants.Login.METHOD, CSNConstants.Login.TRY_LOGIN);
            this.add(CSNConstants.Login.CSN_LOGIN, CSNConstants.Login.BANK_ID);
        }
    }
}
