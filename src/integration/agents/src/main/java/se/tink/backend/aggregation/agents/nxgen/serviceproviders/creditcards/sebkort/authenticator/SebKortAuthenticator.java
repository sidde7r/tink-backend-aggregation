package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.AuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SebKortAuthenticator implements BankIdAuthenticator<BankIdInitResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SebKortAuthenticator.class);

    private final SebKortApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SebKortConfiguration config;

    public SebKortAuthenticator(
            SebKortApiClient apiClient,
            SessionStorage sessionStorage,
            SebKortConfiguration config) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    @Override
    public BankIdInitResponse init(String ssn) {
        final BankIdInitRequest request = new BankIdInitRequest(ssn, config.getApiKey());
        final BankIdInitResponse response = apiClient.initBankId(request);

        return response;
    }

    @Override
    public BankIdStatus collect(BankIdInitResponse reference)
            throws AuthenticationException, AuthorizationException {
        try {
            final BankIdCollectRequest collectRequest =
                    new BankIdCollectRequest(reference.getOrderRef());
            final BankIdCollectResponse collectResponse =
                    apiClient.collectBankId(reference.getCollectUrl(), collectRequest);

            final BankIdStatus bankIdStatus = collectResponse.getBankIdStatus();

            if (bankIdStatus == BankIdStatus.DONE) {
                final BankIdCompleteResponse completeResponse =
                        apiClient.completeBankId(collectResponse.getCompleteUrl());

                final LoginRequest loginRequest =
                        new LoginRequest(completeResponse.getResponseSAML(), config);
                final LoginResponse loginResponse = apiClient.login(loginRequest);

                LOGGER.info("BankID LoginResponse debugString: " + loginResponse.toDebugString());

                final AuthRequest authRequest =
                        new AuthRequest(loginResponse.getUid(), loginResponse.getSecret(), config);
                final AuthResponse authResponse = apiClient.auth(authRequest);

                if (authResponse.isSuccess()) {
                    LOGGER.info(
                            "BankID Login successful "
                                    + SerializationUtils.serializeToString(authResponse));
                    sessionStorage.put(
                            SebKortConstants.StorageKey.AUTHORIZATION,
                            "Bearer " + SebKortConstants.AUTHORIZATION_UUID);
                } else {
                    LOGGER.info(
                            "BankID Login Failed "
                                    + SerializationUtils.serializeToString(authResponse));
                }
            }
            return bankIdStatus;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.INTERRUPTED.exception();
            }
            throw e;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }
}
