package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.BankIdStatusCodes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SbabAuthenticator implements BankIdAuthenticator<BankIdResponse> {

    private final SbabApiClient apiClient;
    private String autoStartToken;
    private OAuth2Token token;
    private PersistentStorage persistentStorage;
    private static final Logger logger = LoggerFactory.getLogger(SbabAuthenticator.class);

    public SbabAuthenticator(SbabApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public BankIdResponse init(String ssn)
            throws BankIdException, BankServiceException, LoginException {
        if (Strings.isNullOrEmpty(ssn)) {
            logger.error("SSN was passed as empty or null!");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        persistentStorage.remove(StorageKeys.PAGINATION_INDICATOR_REFRESHED_TOKEN);
        try {
            BankIdResponse response = apiClient.initBankId(ssn);
            autoStartToken = response.getAutostartToken();
            return response;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }
            throw e;
        }
    }

    @Override
    public BankIdStatus collect(BankIdResponse reference) {
        try {
            DecoupledResponse decoupledResponse =
                    apiClient.getDecoupled(reference.getAuthorizationCode());
            token = decoupledResponse.getAccessToken();
        } catch (HttpResponseException e) {
            if (e.getMessage().contains(BankIdStatusCodes.AUTHORIZATION_NOT_COMPLETED)) {
                return BankIdStatus.WAITING;
            }
            if (e.getMessage().contains(BankIdStatusCodes.USER_NOT_FOUND)) {
                return BankIdStatus.NO_CLIENT;
            }
            if (e.getMessage().contains(BankIdStatusCodes.AUTHORIZATION_FAILED)) {
                return BankIdStatus.CANCELLED;
            }
            return BankIdStatus.FAILED_UNKNOWN;
        }
        return BankIdStatus.DONE;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.ofNullable(token);
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        try {
            Optional<OAuth2Token> refreshedToken =
                    Optional.ofNullable(
                            apiClient.refreshAccessToken(refreshToken).getAccessToken());
            if (refreshedToken.isPresent()) {
                persistentStorage.put(StorageKeys.PAGINATION_INDICATOR_REFRESHED_TOKEN, false);
            }
            return refreshedToken;
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                // BankId Auth controller will throw session expired to force manual login
                return Optional.empty();
            }
            if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                hre.getResponse().getBody(ErrorResponse.class).handleErrors();
            }
            throw hre;
        }
    }
}
