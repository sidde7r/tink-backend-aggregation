package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
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
    public BankIdInitResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        try {
            BankIdInitRequest request = new BankIdInitRequest(ssn, config.getApiKey());
            BankIdInitResponse response = apiClient.initBankId(request);

            if (response.isError()) {
                LOGGER.error(
                        "BankID Signicat error: {} ({})",
                        response.getError().getMessage(),
                        response.getError().getCode());
                throw BankIdError.UNKNOWN.exception();
            }
            return response;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }
            throw e;
        }
    }

    @Override
    public BankIdStatus collect(BankIdInitResponse reference)
            throws AuthenticationException, AuthorizationException {
        try {
            BankIdCollectRequest collectRequest = new BankIdCollectRequest(reference.getOrderRef());
            BankIdCollectResponse collectResponse =
                    apiClient.collectBankId(reference.getCollectUrl(), collectRequest);

            BankIdStatus bankIdStatus = collectResponse.getBankIdStatus();

            if (bankIdStatus == BankIdStatus.DONE) {
                BankIdCompleteResponse completeResponse =
                        apiClient.completeBankId(collectResponse.getCompleteUrl());

                LoginRequest loginRequest =
                        new LoginRequest(completeResponse.getResponseSAML(), config);
                LoginResponse loginResponse = apiClient.login(loginRequest);

                AuthRequest authRequest =
                        new AuthRequest(loginResponse.getUid(), loginResponse.getSecret(), config);
                AuthResponse authResponse = apiClient.auth(authRequest);

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
}
