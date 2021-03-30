package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.BankIdAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.DeviceAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.validators.BankidAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.identity.HandelsbankenSEIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Mandate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.identitydata.IdentityData;

public class HandelsbankenBankIdAuthenticator implements BankIdAuthenticator<InitBankIdResponse> {
    private final HandelsbankenSEApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final HandelsbankenSessionStorage sessionStorage;
    private int pollCount;
    private String autoStartToken;
    private String lastWaitingResult;
    private String givenSsn;

    public HandelsbankenBankIdAuthenticator(
            HandelsbankenSEApiClient client,
            HandelsbankenPersistentStorage persistentStorage,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public InitBankIdResponse init(String ssn) throws BankIdException, AuthorizationException {
        pollCount = 0;
        this.givenSsn = ssn;
        return refreshAutostartToken();
    }

    @Override
    public BankIdStatus collect(InitBankIdResponse initBankId)
            throws AuthenticationException, AuthorizationException {
        if (!Strings.isNullOrEmpty(initBankId.getCode())) {
            // if a bankid signature is running at the time we initiate ours the bank/bankid will
            // cancel both of them.
            if (HandelsbankenSEConstants.BankIdAuthentication.CANCELLED.equalsIgnoreCase(
                    initBankId.getCode())) {
                return BankIdStatus.CANCELLED;
            }
        }

        AuthenticateResponse authenticate = client.authenticate(initBankId);
        BankIdStatus bankIdStatus = authenticate.toBankIdStatus();
        switch (bankIdStatus) {
            case DONE:
                AuthorizeResponse authorize = client.authorize(authenticate);

                new BankidAuthenticationValidator(authorize).validate();

                // If SSN is given, check that it matches the logged in user
                if (!Strings.isNullOrEmpty(this.givenSsn)) {
                    checkIdentity(this.givenSsn, authorize.getMandates());
                }

                ApplicationEntryPointResponse applicationEntryPoint =
                        client.applicationEntryPoint(authorize);

                persistentStorage.persist(authorize);
                sessionStorage.persist(applicationEntryPoint);
                client.keepAlive(applicationEntryPoint);
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

    private void checkIdentity(String ssn, List<Mandate> mandates) throws LoginException {
        final String identitySsn =
                HandelsbankenSEIdentityFetcher.mandatesToIdentity(mandates)
                        .map(IdentityData::getSsn)
                        .orElse("");

        if (!identitySsn.equalsIgnoreCase(ssn)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
