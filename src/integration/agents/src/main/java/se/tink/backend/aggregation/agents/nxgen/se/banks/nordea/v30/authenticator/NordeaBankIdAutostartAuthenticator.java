package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator;

import java.util.Optional;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.NordeaBankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.BankIdAutostartResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.FetchCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.InitBankIdAutostartRequest;
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
    private String state;
    private String nonce;
    private String codeVerifier;
    private String codeChallenge;
    private String autoStartToken;

    public NordeaBankIdAutostartAuthenticator(
            NordeaSEApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public BankIdAutostartResponse init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
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
        this.state = Base64.encodeBase64URLSafeString(RandomUtils.secureRandom(19));
        this.nonce = Base64.encodeBase64URLSafeString(RandomUtils.secureRandom(19));
        this.codeChallenge = createCodeChallenge();

        return new InitBankIdAutostartRequest()
                .setState(state)
                .setNonce(nonce)
                .setCodeChallenge(codeChallenge);
    }

    private String createCodeChallenge() {
        byte[] randomCodeChallengeBytes = RandomUtils.secureRandom(64);
        this.codeVerifier = Base64.encodeBase64URLSafeString(randomCodeChallengeBytes);

        return Base64.encodeBase64URLSafeString(Hash.sha256(codeVerifier));
    }

    private BankIdStatus fetchAccessToken(BankIdAutostartResponse bankIdAutostartResponse) {
        try {
            final FetchCodeRequest fetchCodeRequest =
                    new FetchCodeRequest().setCode(bankIdAutostartResponse.getCode());
            bankIdAutostartResponse = apiClient.fetchLoginCode(fetchCodeRequest);

            apiClient
                    .fetchAccessToken(bankIdAutostartResponse.getCode(), codeVerifier)
                    .storeTokens(sessionStorage);
        } catch (HttpResponseException e) {
            return BankIdStatus.NO_CLIENT;
        }
        return BankIdStatus.DONE;
    }
}
