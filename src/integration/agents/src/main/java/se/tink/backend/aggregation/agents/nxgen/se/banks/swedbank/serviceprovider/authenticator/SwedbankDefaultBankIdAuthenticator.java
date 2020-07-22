package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.AbstractBankIdAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
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
    private SwedbankBaseConstants.BankIdResponseStatus previousStatus;
    private final SessionStorage sessionStorage;

    public SwedbankDefaultBankIdAuthenticator(
            SwedbankDefaultApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public AbstractBankIdAuthResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        previousStatus = null;
        InitBankIdResponse initBankIdResponse = initBankId(ssn);

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

            previousStatus = bankIdResponseStatus;

            switch (bankIdResponseStatus) {
                case CLIENT_NOT_STARTED:
                case USER_SIGN:
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
            HttpResponse httpResponse = hre.getResponse();
            // when timing out, this can also be the response
            int responseStatus = httpResponse.getStatus();
            if (responseStatus == HttpStatus.SC_UNAUTHORIZED) {
                ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
                if (errorResponse.hasErrorCode(
                        SwedbankBaseConstants.BankErrorMessage.LOGIN_FAILED)) {
                    return BankIdStatus.TIMEOUT;
                } else if (errorResponse.hasErrorCode(
                        SwedbankBaseConstants.BankErrorMessage.SESSION_INVALIDATED)) {
                    return BankIdStatus.FAILED_UNKNOWN;
                }
            } else if (responseStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                // This code is a temporary fix until Swedbank returns a better error message.
                // What we belive to be the problem is that when multiple request are sent to bankid
                // at the same time
                // bankid cancels all requests.
                if (previousStatus
                        == SwedbankBaseConstants.BankIdResponseStatus.CLIENT_NOT_STARTED) {
                    return BankIdStatus.INTERRUPTED;
                }
            }

            // unknown error re-throw
            throw hre;
        }
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
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    private void completeBankIdLogin(CollectBankIdResponse collectBankIdResponse)
            throws AuthenticationException {
        final LinkEntity nextLink = collectBankIdResponse.getLinks().getNextOrThrow();
        try {
            apiClient.completeAuthentication(nextLink);
        } catch (HttpResponseException hre) {
            // wait and retry once on SESSION_INVALIDATED error
            if (isSessionInvalidatedError(hre.getResponse())) {
                log.warn("Got session invalidated, retrying.");
                Uninterruptibles.sleepUninterruptibly(900, TimeUnit.MILLISECONDS);
                apiClient.completeAuthentication(nextLink);
                return;
            }
            throw hre;
        }
    }

    private InitBankIdResponse initBankId(String ssn) throws BankIdException {
        try {
            return apiClient.initBankId(ssn);
        } catch (HttpResponseException hre) {
            HttpResponse httpResponse = hre.getResponse();
            if (httpResponse.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);

                if (errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.USER_ID)) {
                    throw BankIdError.USER_VALIDATION_ERROR.exception(hre);
                }
            }

            throw hre;
        }
    }

    private boolean isSessionInvalidatedError(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_UNAUTHORIZED
                && response.getBody(ErrorResponse.class)
                        .hasErrorCode(SwedbankBaseConstants.BankErrorMessage.SESSION_INVALIDATED);
    }
}
