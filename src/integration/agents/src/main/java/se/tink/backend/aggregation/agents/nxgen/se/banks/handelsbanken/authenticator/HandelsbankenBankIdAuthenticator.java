package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.DeviceAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.validators.BankidAuthenticationValidator;
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
        InitBankIdRequest initBankIdRequest =
                new InitBankIdRequest().setBidDevice(DeviceAuthentication.DEVICE_ID);
        InitBankIdResponse response = client.initToBank(initBankIdRequest);
        autoStartToken = response.getAutoStartToken();
        return response;
    }

    @Override
    public BankIdStatus collect(InitBankIdResponse initBankId)
            throws AuthenticationException, AuthorizationException {

        AuthenticateResponse authenticate = client.authenticate(initBankId);
        BankIdStatus bankIdStatus = authenticate.toBankIdStatus();
        if (bankIdStatus == BankIdStatus.DONE) {
            AuthorizeResponse authorize = client.authorize(authenticate);

            new BankidAuthenticationValidator(credentials, authorize).validate();

            ApplicationEntryPointResponse applicationEntryPoint =
                    client.applicationEntryPoint(authorize);

            persistentStorage.persist(authorize);
            sessionStorage.persist(applicationEntryPoint);
        } else if (bankIdStatus == BankIdStatus.WAITING) {
            pollCount++;
        } else if (bankIdStatus == BankIdStatus.TIMEOUT && pollCount < 10) {
            return BankIdStatus.FAILED_UNKNOWN;
        }

        return bankIdStatus;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAcessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }
}
