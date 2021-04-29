package se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNAuthSessionStorageHelper;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid.rpc.LoginForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RequiredArgsConstructor
public class CSNBankIdAuthenticator implements BankIdAuthenticator<String> {
    private final CSNApiClient apiClient;
    private final CSNAuthSessionStorageHelper authSessionStorageHelper;

    @Override
    public String init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        final HttpResponse loginResponse = apiClient.initBankId(new LoginForm(ssn.substring(2)));
        if (loginResponse.getBody(String.class).contains("Fel på sidan")) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }

        return apiClient.extractSessionId(loginResponse);
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
                authSessionStorageHelper.storeAccessToken(apiClient.extractAccessToken());
                return BankIdStatus.DONE;
            default:
                return BankIdStatus.TIMEOUT;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }
}
