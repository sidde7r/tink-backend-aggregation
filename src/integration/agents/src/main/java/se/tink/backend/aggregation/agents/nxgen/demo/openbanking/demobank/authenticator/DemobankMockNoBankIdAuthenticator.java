package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.NoBankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.NoBankIdInitResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

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
        NoBankIdInitResponse bankIdInitResponse = apiClient.initBankIdNo(nationalId, mobilenumber);
        if (bankIdInitResponse.isAlreadyInProgress()) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }
        this.ssn = nationalId;
        this.sessionId = bankIdInitResponse.getSessionId();
        return bankIdInitResponse.getSessionId();
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        NoBankIdCollectResponse bankIdCollectResponse =
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
