package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.AbstractBankIdAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileResponse;
import se.tink.backend.aggregation.agents.utils.business.OrganisationNumberSeLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SwedbankDefaultBankIdAuthenticator
        implements BankIdAuthenticator<AbstractBankIdAuthResponse> {
    private static final Logger log =
            LoggerFactory.getLogger(SwedbankDefaultBankIdAuthenticator.class);

    private final SwedbankDefaultApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String organisationNumber;

    private SwedbankBaseConstants.BankIdResponseStatus previousStatus;
    private String givenSsn;
    private String autoStartToken;
    private int pollCount;

    public SwedbankDefaultBankIdAuthenticator(
            SwedbankDefaultApiClient apiClient,
            SessionStorage sessionStorage,
            String organisationNumber) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.organisationNumber = organisationNumber;
    }

    @Override
    public AbstractBankIdAuthResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        this.previousStatus = null;
        this.givenSsn = ssn;
        this.pollCount = 0;

        OrganisationNumberSeLogger.logIfUnknownOrgnumber(organisationNumber);

        InitBankIdResponse initBankIdResponse = refreshAutostartToken();

        LinkEntity linkEntity = initBankIdResponse.getLinks().getNextOrThrow();
        Preconditions.checkState(
                linkEntity.isValid(),
                "Login failed - Cannot proceed without valid link entity - Method:{}, Uri:{}",
                linkEntity.getMethod(),
                linkEntity.getUri());

        if (initBankIdResponse.getBankIdStatus()
                == SwedbankBaseConstants.BankIdResponseStatus.ALREADY_IN_PROGRESS) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }

        return initBankIdResponse;
    }

    @Override
    public BankIdStatus collect(AbstractBankIdAuthResponse response)
            throws AuthenticationException, AuthorizationException {
        try {
            CollectBankIdResponse collectBankIdResponse =
                    apiClient.collectBankId(response.getLinks().getNextOrThrow());
            SwedbankBaseConstants.BankIdResponseStatus bankIdResponseStatus =
                    collectBankIdResponse.getBankIdStatus();

            this.previousStatus = bankIdResponseStatus;

            switch (bankIdResponseStatus) {
                case CLIENT_NOT_STARTED:
                case USER_SIGN:
                    pollCount++;
                    return BankIdStatus.WAITING;
                case CANCELLED:
                    return BankIdStatus.CANCELLED;
                case INTERRUPTED:
                    return BankIdStatus.INTERRUPTED;
                case COMPLETE:
                    // Store if bankID has extended usage for when fetching transfer destinations
                    sessionStorage.put(
                            StorageKey.HAS_EXTENDED_USAGE, collectBankIdResponse.isExtendedUsage());
                    completeBankIdLogin(collectBankIdResponse);
                    return BankIdStatus.DONE;
                case TIMEOUT:
                    return BankIdStatus.TIMEOUT;
                default:
                    log.warn(
                            "Login failed - Not implemented case - BankIdResponseStatus:{} from {}",
                            bankIdResponseStatus,
                            collectBankIdResponse.getStatus());
                    throw new IllegalStateException(
                            "Login failed - Cannot proceed with unknown bankId status");
            }
        } catch (HttpResponseException hre) {
            return handleBankIdErrors(hre);
        }
    }

    private BankIdStatus handleBankIdErrors(HttpResponseException hre) {
        HttpResponse httpResponse = hre.getResponse();

        if (httpResponse.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
            ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
            if (errorResponse.isLoginFailedError()) {
                if (pollCount < 10) {
                    return BankIdStatus.FAILED_UNKNOWN;
                }

                if (previousStatusWasUserSign() || pollCount > 35) {
                    return BankIdStatus.TIMEOUT;
                }

                if (previousStatusWasClientNotStarted()) {
                    return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
                }
            } else if (errorResponse.isSessionInvalidatedError()) {
                // When user has bank app running, and starts a refresh, both sessions will be
                // invalidated
                throw SessionError.SESSION_ALREADY_ACTIVE.exception();
            }
        }

        // unknown error re-throw
        throw hre;
    }

    private boolean previousStatusWasUserSign() {
        return SwedbankBaseConstants.BankIdResponseStatus.USER_SIGN.equals(previousStatus);
    }

    private boolean previousStatusWasClientNotStarted() {
        return SwedbankBaseConstants.BankIdResponseStatus.CLIENT_NOT_STARTED.equals(previousStatus);
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public InitBankIdResponse refreshAutostartToken() throws BankServiceException {
        InitBankIdResponse initBankIdResponse = apiClient.initBankId();
        this.autoStartToken = initBankIdResponse.getAutoStartToken();

        return initBankIdResponse;
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    private void completeBankIdLogin(CollectBankIdResponse collectBankIdResponse)
            throws AuthenticationException {
        ProfileResponse profileResponse =
                apiClient.completeAuthentication(collectBankIdResponse.getLinks().getNextOrThrow());

        // If SSN is given, check that it matches the authenticated user
        if (!Strings.isNullOrEmpty(this.givenSsn)) {
            verifyIdentity(profileResponse);
        }

        OrganisationNumberSeLogger.logIfUnknownOrgnumberForSuccessfulLogin(organisationNumber);
    }

    private void verifyIdentity(ProfileResponse profileResponse) {
        if (!this.givenSsn.equalsIgnoreCase(profileResponse.getUserId())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
