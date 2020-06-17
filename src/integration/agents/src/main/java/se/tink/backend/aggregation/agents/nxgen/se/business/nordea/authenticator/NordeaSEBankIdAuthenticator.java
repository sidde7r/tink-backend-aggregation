package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.ResultBankIdResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSEBankIdAuthenticator implements BankIdAuthenticator<String> {
    private final NordeaSEApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String orgNumber;

    public NordeaSEBankIdAuthenticator(
            NordeaSEApiClient apiClient, SessionStorage sessionStorage, String orgNumber) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.orgNumber = orgNumber;
    }

    @Override
    public String init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException,
                    AuthenticationException {
        final InitBankIdRequest initBankIdRequest = new InitBankIdRequest(ssn);
        final InitBankIdResponse initBankIdResponse = apiClient.initBankId(initBankIdRequest);
        sessionStorage.put(StorageKeys.SECURITY_TOKEN, initBankIdResponse.getSecurityToken());

        return initBankIdResponse.getReference();
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        final ResultBankIdResponse resultBankIdResponse = apiClient.resultBankId(reference);

        final Optional<String> errorCode = resultBankIdResponse.getErrorCode();
        if (errorCode.isPresent()) {
            if (ErrorCodes.NO_CLIENT.equals(errorCode.get().toUpperCase())) {
                return BankIdStatus.NO_CLIENT;
            }
            return BankIdStatus.FAILED_UNKNOWN;
        }
        switch (resultBankIdResponse.getBankIdStatus().toUpperCase()) {
            case NordeaSEConstants.BankIdStatus.COMPLETE:
                fetchToken(resultBankIdResponse);
                return BankIdStatus.DONE;
            case NordeaSEConstants.BankIdStatus.WAITING:
            case NordeaSEConstants.BankIdStatus.USER_SIGNING:
                return BankIdStatus.WAITING;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private void fetchToken(ResultBankIdResponse resultBankIdResponse) {
        String securityToken = resultBankIdResponse.getToken();
        sessionStorage.put(StorageKeys.SECURITY_TOKEN, securityToken);

        final String holderName = resultBankIdResponse.getHolderName();
        sessionStorage.put(StorageKeys.HOLDER_NAME, holderName);

        final String id = resultBankIdResponse.getId(orgNumber);
        final FetchTokenRequest tokenRequest = new FetchTokenRequest(id);
        final FetchTokenResponse tokenResponse = apiClient.fetchToken(tokenRequest);
        securityToken = tokenResponse.getToken();
        sessionStorage.put(StorageKeys.SECURITY_TOKEN, securityToken);
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) throws SessionException {
        return Optional.empty();
    }
}
