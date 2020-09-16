package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator;

import java.util.Optional;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.NordeaBankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.BankIdAutostartResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdAutostartRequest;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaBankIdAutostartAuthenticator
        implements BankIdAuthenticator<BankIdAutostartResponse> {
    private final NordeaSEApiClient apiClient;
    private final SessionStorage sessionStorage;
    private String ssn;
    private String codeVerifier;
    private String autoStartToken;

    public NordeaBankIdAutostartAuthenticator(
            NordeaSEApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public BankIdAutostartResponse init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        this.ssn = ssn;
        checkIdentity(ssn);
        return refreshAutostartToken();
    }

    @Override
    public BankIdStatus collect(BankIdAutostartResponse reference)
            throws AuthenticationException, AuthorizationException {
        try {
            BankIdAutostartResponse bankIdAutostartResponse =
                    apiClient.pollBankIdAutostart(reference.getSessionId());
            switch (bankIdAutostartResponse.getStatus().toLowerCase()) {
                case NordeaBankIdStatus.BANKID_AUTOSTART_PENDING:
                case NordeaBankIdStatus.BANKID_AUTOSTART_SIGN_PENDING:
                    return BankIdStatus.WAITING;
                case NordeaBankIdStatus.BANKID_AUTOSTART_COMPLETED:
                    return fetchAccessToken(bankIdAutostartResponse);
                case NordeaBankIdStatus.BANKID_AUTOSTART_CANCELLED:
                    return BankIdStatus.CANCELLED;
                default:
                    return BankIdStatus.FAILED_UNKNOWN;
            }
        } catch (HttpResponseException e) {
            return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
        }
    }

    @Override
    public BankIdAutostartResponse refreshAutostartToken() throws BankServiceException {
        final InitBankIdAutostartRequest initBankIdAutostartRequest =
                createInitBankIdAutostartRequest();
        final BankIdAutostartResponse bankIdAutostartResponse =
                apiClient.initBankIdAutostart(initBankIdAutostartRequest);
        this.autoStartToken = bankIdAutostartResponse.getAutoStartToken();

        return bankIdAutostartResponse;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    private InitBankIdAutostartRequest createInitBankIdAutostartRequest() {
        return new InitBankIdAutostartRequest(
                Base64.encodeBase64URLSafeString(RandomUtils.secureRandom(19)),
                Base64.encodeBase64URLSafeString(RandomUtils.secureRandom(19)),
                createCodeChallenge());
    }

    private String createCodeChallenge() {
        byte[] randomCodeChallengeBytes = RandomUtils.secureRandom(64);
        this.codeVerifier = Base64.encodeBase64URLSafeString(randomCodeChallengeBytes);

        return Base64.encodeBase64URLSafeString(Hash.sha256(codeVerifier));
    }

    private BankIdStatus fetchAccessToken(BankIdAutostartResponse bankIdAutostartResponse)
            throws LoginException {
        try {
            final FetchCodeRequest fetchCodeRequest =
                    new FetchCodeRequest(bankIdAutostartResponse.getCode());
            bankIdAutostartResponse = apiClient.fetchLoginCode(fetchCodeRequest);

            apiClient
                    .fetchAccessToken(bankIdAutostartResponse.getCode(), codeVerifier)
                    .storeTokens(sessionStorage);
        } catch (HttpResponseException e) {
            return BankIdStatus.NO_CLIENT;
        }
        return BankIdStatus.DONE;
    }

    private void checkIdentity(String ssn) throws LoginException {
        if (!this.ssn.equalsIgnoreCase(ssn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
