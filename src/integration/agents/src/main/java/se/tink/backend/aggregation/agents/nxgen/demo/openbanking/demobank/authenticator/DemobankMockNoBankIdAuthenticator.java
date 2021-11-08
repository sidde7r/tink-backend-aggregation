package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DemobankMockNoBankIdAuthenticator implements BankIdAuthenticatorNO {
    public DemobankMockNoBankIdAuthenticator(DemobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private DemobankApiClient apiClient;
    private String ssn;
    private String sessionId;

    @Override
    public String init(String nationalId, String dob, String mobilenumber)
            throws AuthenticationException, AuthorizationException {
        BankIdInitResponse bankIdInitResponse;
        try {
            bankIdInitResponse = apiClient.initBankIdNo(nationalId, mobilenumber);
        } catch (HttpResponseException e) {
            handleInitErrors(e);
            throw e;
        }

        if (bankIdInitResponse.isAlreadyInProgress()) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }
        this.ssn = nationalId;
        this.sessionId = bankIdInitResponse.getSessionId();
        return bankIdInitResponse.getSessionId();
    }

    private void handleInitErrors(HttpResponseException exception) throws AuthenticationException {
        HttpResponse response = exception.getResponse();
        if (response.getStatus() != 400) {
            return;
        }

        String body = response.getBody(String.class);
        if (Strings.isNullOrEmpty(body)) {
            return;
        }

        String lowerCaseBody = body.toLowerCase();
        if (lowerCaseBody.contains("ssn not found")
                || lowerCaseBody.contains("mobile number not matching")) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(exception);
        }
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        BankIdCollectResponse bankIdCollectResponse =
                apiClient.collectBankIdNo(this.ssn, this.sessionId);
        BankIdStatus status = bankIdCollectResponse.getBankIdStatus();
        if (BankIdStatus.DONE.equals(status)) {
            apiClient.setTokenToStorage(
                    OAuth2Token.createBearer(
                            bankIdCollectResponse.getToken(),
                            bankIdCollectResponse.getToken(),
                            3600));
        }
        return status;
    }
}
