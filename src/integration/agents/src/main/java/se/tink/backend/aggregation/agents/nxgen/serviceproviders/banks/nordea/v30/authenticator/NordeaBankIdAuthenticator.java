package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.NordeaBankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.BankIdAutostartResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.FetchCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.FetchCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.InitBankIdAutostartRequest;
import se.tink.backend.aggregation.agents.utils.business.OrganisationNumberSeLogger;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class NordeaBankIdAuthenticator implements BankIdAuthenticator<BankIdAutostartResponse> {
    private final NordeaBaseApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String organisationNumber;

    private String givenSsn;
    private String state;
    private String nonce;
    private String codeVerifier;
    private String codeChallenge;
    private String autoStartToken;
    private NordeaConfiguration nordeaConfiguration;

    public NordeaBankIdAuthenticator(
            NordeaBaseApiClient apiClient,
            SessionStorage sessionStorage,
            NordeaConfiguration nordeaConfiguration,
            String organisationNumber) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.nordeaConfiguration = nordeaConfiguration;
        this.organisationNumber = organisationNumber;
    }

    @Override
    public BankIdAutostartResponse init(String ssn)
            throws BankServiceException, AuthorizationException, AuthenticationException {
        this.givenSsn = ssn;

        if (nordeaConfiguration.isBusinessAgent()) {
            OrganisationNumberSeLogger.logIfUnknownOrgnumber(organisationNumber);
        }

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

    private InitBankIdAutostartRequest createInitBankIdAutostartRequest() {
        this.state = Base64.encodeBase64URLSafeString(RandomUtils.secureRandom(19));
        this.nonce = Base64.encodeBase64URLSafeString(RandomUtils.secureRandom(19));
        this.codeChallenge = createCodeChallenge();

        return new InitBankIdAutostartRequest()
                .setState(state)
                .setNonce(nonce)
                .setCodeChallenge(codeChallenge)
                .setRedirectUri(nordeaConfiguration.getRedirectUri())
                .setClientId(nordeaConfiguration.getClientId());
    }

    private String createCodeChallenge() {
        byte[] randomCodeChallengeBytes = RandomUtils.secureRandom(64);
        this.codeVerifier = Base64.encodeBase64URLSafeString(randomCodeChallengeBytes);

        return Base64.encodeBase64URLSafeString(Hash.sha256(codeVerifier));
    }

    private BankIdStatus fetchAccessToken(BankIdAutostartResponse bankIdAutostartResponse)
            throws LoginException {

        final FetchCodeRequest fetchCodeRequest =
                new FetchCodeRequest().setCode(bankIdAutostartResponse.getCode());

        FetchCodeResponse fetchCodeResponse = apiClient.fetchLoginCode(fetchCodeRequest);

        if (Strings.isNullOrEmpty(fetchCodeResponse.getCode())) {
            throw new IllegalStateException("Login code not present, can't fetch access token.");
        }

        apiClient
                .fetchAccessToken(fetchCodeResponse.getCode(), codeVerifier)
                .storeTokens(sessionStorage);

        // If SSN is given, check that it matches the logged in user
        if (!Strings.isNullOrEmpty(this.givenSsn) && !nordeaConfiguration.isBusinessAgent()) {
            checkIdentity();
        }

        if (nordeaConfiguration.isBusinessAgent()) {
            OrganisationNumberSeLogger.logIfUnknownOrgnumberForSuccessfulLogin(organisationNumber);
        }

        return BankIdStatus.DONE;
    }

    private void checkIdentity() throws LoginException {
        final IdentityData identityData = apiClient.fetchIdentityData().toPrivateIdentityData();
        if (!identityData.getSsn().equalsIgnoreCase(this.givenSsn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
