package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.SessionBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcabankenPersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.uuid.UUIDUtils;

public class IcaBankenBankIdAuthenticator implements BankIdAuthenticator<String> {
    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage icaBankenSessionStorage;
    private final IcabankenPersistentStorage icabankenPersistentStorage;

    private String autostarttoken;
    private int pollCounter = 0;

    public IcaBankenBankIdAuthenticator(
            IcaBankenApiClient apiClient,
            IcaBankenSessionStorage icaBankenSessionStorage,
            IcabankenPersistentStorage icabankenPersistentStorage) {
        this.apiClient = apiClient;
        this.icaBankenSessionStorage = icaBankenSessionStorage;
        this.icabankenPersistentStorage = icabankenPersistentStorage;
    }

    @Override
    public String init(String ssn) throws BankIdException {
        try {
            BankIdBodyEntity response = apiClient.initBankId(ssn);
            autostarttoken = response.getAutostartToken();
            return response.getRequestId();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            } else if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }

            throw e;
        }
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {

        BankIdResponse response = getPollResponse(reference);
        BankIdStatus bankIdStatus = response.getBankIdStatus();

        if (bankIdStatus == BankIdStatus.DONE) {
            icaBankenSessionStorage.saveSessionId(response.getBody().getSessionId());

            persistNewDeviceApplicationIdIfMissing();

            SessionBodyEntity sessionBodyEntity = apiClient.fetchSessionInfo();

            if (sessionBodyEntity.mustUpdateInformationAtBank()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        IcaBankenConstants.UserMessage.KNOW_YOUR_CUSTOMER.getKey());
            }

            persistUserInstallationIdIfMissing(sessionBodyEntity);
        }

        pollCounter++;
        return bankIdStatus;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autostarttoken);
    }

    @Override
    public Optional<OAuth2Token> getAcessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }

    private BankIdResponse getPollResponse(String reference)
            throws AuthenticationException, AuthorizationException {

        try {
            return apiClient.pollBankId(reference);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();

            if (response != null && response.getStatus() == HttpStatus.SC_CONFLICT) {
                handleKnownErrors(response);
            }

            throw e;
        }
    }

    private void handleKnownErrors(HttpResponse response)
            throws AuthenticationException, AuthorizationException {

        BankIdResponse bankIdResponse = response.getBody(BankIdResponse.class);

        BankIdBodyEntity bankIdBodyEntity = bankIdResponse.getBody();
        ResponseStatusEntity responseStatus = bankIdResponse.getResponseStatus();

        // If body is empty we look for error cause in the response status
        if (bankIdBodyEntity.getStatus() == null) {

            if (responseStatus.isNotACustomer()) {
                throw LoginError.NOT_CUSTOMER.exception();
            }

            if (responseStatus.isInterrupted()) {
                throw BankIdError.INTERRUPTED.exception();
            }

            if (responseStatus.isNotVerified()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            }
        }

        if (bankIdBodyEntity.isTimeOut()) {
            throw BankIdError.TIMEOUT.exception();
        }

        // We sometimes see these temporary errors from Icabanken that we have deemed to be on their
        // side
        // as users that have gotten this error have manged to update at a later time. Setting a
        // condition
        // that we have managed to poll at least once before getting the error. Getting errors on
        // first poll
        // may indicate that something's wrong on our side.
        if (bankIdBodyEntity.isFailed()
                && responseStatus.isSomethingWentWrong()
                && pollCounter > 0) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    /**
     * The application id seems to be random generated from the app installation UUID, so if this is
     * first time for the credential we won't have any deviceApplicationId stored from before. Then
     * generate one for this credential.
     *
     * <p>This ID is later used as query param when upon fetching the SessionResponse.
     */
    private void persistNewDeviceApplicationIdIfMissing() {
        String deviceApplicationId = icabankenPersistentStorage.getDeviceApplicationId();

        if (Strings.isNullOrEmpty(deviceApplicationId)) {
            deviceApplicationId = UUIDUtils.generateUUID();
            icabankenPersistentStorage.saveDeviceApplicationId(deviceApplicationId);
        }
    }

    /** After the userInstallationId has been set it is added to the headers of all requests. */
    private void persistUserInstallationIdIfMissing(SessionBodyEntity sessionBodyEntity) {
        String userInstallationId = icabankenPersistentStorage.getUserInstallationId();

        if (Strings.isNullOrEmpty(userInstallationId)) {
            userInstallationId = sessionBodyEntity.getCustomer().getUserInstallationId();
            icabankenPersistentStorage.saveUserInstallationId(userInstallationId);
        }
    }
}
