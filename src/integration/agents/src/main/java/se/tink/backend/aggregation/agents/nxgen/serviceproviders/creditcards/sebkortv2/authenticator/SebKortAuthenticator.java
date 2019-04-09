package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.AuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

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

                final AuthRequest authRequest =
                        new AuthRequest(loginResponse.getUid(), loginResponse.getSecret(), config);
                final AuthResponse authResponse = apiClient.auth(authRequest);

                if (authResponse.isSuccess()) {
                    sessionStorage.put(
                            SebKortConstants.StorageKey.AUTHORIZATION,
                            "Bearer " + SebKortConstants.AUTHORIZATION_UUID);
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
