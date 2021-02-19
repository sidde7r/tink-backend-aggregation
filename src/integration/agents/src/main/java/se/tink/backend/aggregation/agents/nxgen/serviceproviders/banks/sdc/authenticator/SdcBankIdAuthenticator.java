package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SdcBankIdAuthenticator implements BankIdAuthenticator<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SdcBankIdAuthenticator.class);

    private final SdcApiClient bankClient;
    private final SdcSessionStorage sessionStorage;
    private final Credentials credentials;

    public SdcBankIdAuthenticator(
            SdcApiClient bankClient, SdcSessionStorage sessionStorage, Credentials credentials) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public String init(String ssn) throws BankIdException, AuthorizationException {

        // Sparbanken Syd app does this request and receives 500 in response, this doesn't
        // result in an error in the app. Logging a warning but continuing with authentication.
        try {
            bankClient.initSession();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                LOGGER.warn("SDC SE: Received 500 response on init session request.", e);
            }
        }

        bankClient.initBankId(ssn);

        return "";
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {
        // SDC doesn't poll the bankID, if we try to fetch the agreements before the user has
        // signed we get a 500 response. We can't "poll" using the agreements endpoint, if we get
        // 500 in response that session is killed. Therefore added a super arbitrary sleep of 30 sec
        // before trying to fetch the agreements.
        Uninterruptibles.sleepUninterruptibly(30, TimeUnit.SECONDS);

        try {
            AgreementsResponse agreementsResponse = bankClient.fetchAgreements();

            if (agreementsResponse.isEmpty()) {
                return BankIdStatus.TIMEOUT;
            }

            // if we can retrieve the agreements, we are good.
            sessionStorage.setAgreements(agreementsResponse.toSessionStorageAgreements());
            return BankIdStatus.DONE;
        } catch (Exception e) {
            if (credentials.getUpdated() == null) {
                throw LoginError.NOT_CUSTOMER.exception(e);
            }

            throw e;
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
}
