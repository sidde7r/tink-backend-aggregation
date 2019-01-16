package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.validators.BankidAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.EntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.rpc.Credentials;

public class HandelsbankenBankIdAuthenticator implements BankIdAuthenticator<InitBankIdResponse> {
    private final HandelsbankenSEApiClient client;
    private final Credentials credentials;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenBankIdAuthenticator(HandelsbankenSEApiClient client, Credentials credentials,
            HandelsbankenPersistentStorage persistentStorage,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public InitBankIdResponse init(String ssn) throws BankIdException, AuthorizationException {
        EntryPointResponse entryPoint = client.fetchEntryPoint();
        InitBankIdRequest initBankIdRequest = new InitBankIdRequest()
                .setPersonalNumber(ssn);
        return client.initBankId(entryPoint, initBankIdRequest)
                .validate(() -> client.initBankId(entryPoint, initBankIdRequest));
    }

    @Override
    public BankIdStatus collect(InitBankIdResponse initBankId) throws AuthenticationException, AuthorizationException {
        if (!Strings.isNullOrEmpty(initBankId.getCode())) {
            // if a bankid signature is running at the time we initiate ours the bank/bankid will cancel both of them.
            if (HandelsbankenSEConstants.BankIdAuthentication.CANCELLED.equalsIgnoreCase(initBankId.getCode())) {
                return BankIdStatus.CANCELLED;
            }
        }

        AuthenticateResponse authenticate = client.authenticate(initBankId);
        BankIdStatus bankIdStatus = authenticate.toBankIdStatus();
        if (bankIdStatus == BankIdStatus.DONE) {
            AuthorizeResponse authorize = client.authorize(authenticate);

            new BankidAuthenticationValidator(credentials, authorize).validate();

            ApplicationEntryPointResponse applicationEntryPoint = client.applicationEntryPoint(authorize);

            persistentStorage.persist(authorize);
            sessionStorage.persist(applicationEntryPoint);
        }
        return bankIdStatus;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }
}
