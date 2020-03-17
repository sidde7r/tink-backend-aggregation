package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.BankIdAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.DeviceAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class HandelsbankenBankIdAuthenticator implements BankIdAuthenticator<InitBankIdResponse> {
    private final HandelsbankenSEApiClient client;
    private final Credentials credentials;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final HandelsbankenSessionStorage sessionStorage;
    private int pollCount;
    private String autoStartToken;
    private String lastWaitingResult;

    public HandelsbankenBankIdAuthenticator(
            HandelsbankenSEApiClient client,
            Credentials credentials,
            HandelsbankenPersistentStorage persistentStorage,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public InitBankIdResponse init(String ssn) throws BankIdException, AuthorizationException {
        pollCount = 0;
        return refreshAutostartToken();
    }

    @Override
    public BankIdStatus collect(InitBankIdResponse initBankId)
            throws AuthenticationException, AuthorizationException {
        if (!Strings.isNullOrEmpty(initBankId.getCode())) {
            // if a bankid signature is running at the time we initiate ours the bank/bankid will
            // cancel both of them.
            if (BankIdAuthentication.CANCELLED.equalsIgnoreCase(initBankId.getCode())) {
                return BankIdStatus.CANCELLED;
            }
        }

        AuthenticateResponse authenticate = client.authenticate(initBankId);
        BankIdStatus bankIdStatus = authenticate.toBankIdStatus();
        switch (bankIdStatus) {
            case DONE:
                AuthorizeResponse authorize = client.authorize(authenticate);

                ApplicationEntryPointResponse applicationEntryPoint =
                        client.applicationEntryPoint(authorize);

                persistentStorage.persist(authorize);
                sessionStorage.persist(applicationEntryPoint);
                break;
            case WAITING:
                lastWaitingResult = authenticate.getResult();
                pollCount++;
                break;
            case TIMEOUT:
                if (pollCount < 10) {
                    return BankIdStatus.FAILED_UNKNOWN;
                }
                if (BankIdAuthentication.NO_CLIENT.equals(lastWaitingResult)) {
                    lastWaitingResult = null;
                    return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
                }
                break;
        }
        return bankIdStatus;
    }

    @Override
    public InitBankIdResponse refreshAutostartToken() throws BankServiceException {
        InitBankIdRequest initBankIdRequest =
                new InitBankIdRequest().setBidDevice(DeviceAuthentication.DEVICE_ID);
        InitBankIdResponse response = client.initToBank(initBankIdRequest);
        autoStartToken = response.getAutoStartToken();
        return response;
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
}
