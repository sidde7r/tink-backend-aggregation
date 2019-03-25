package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SdcBankIdAuthenticator implements BankIdAuthenticator<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SdcBankIdAuthenticator.class);

    private final SdcApiClient bankClient;
    private final SdcSessionStorage sessionStorage;
    private final Credentials credentials;

    public SdcBankIdAuthenticator(SdcApiClient bankClient, SdcSessionStorage sessionStorage, Credentials credentials) {
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
                LOGGER.warn("SDC SE: Received 500 response on init session request.");
            }
        }

        bankClient.initBankId(ssn);

        return "";
    }

    @Override
    public BankIdStatus collect(String reference) throws AuthenticationException, AuthorizationException {
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
                throw LoginError.NOT_CUSTOMER.exception();
            }

            throw e;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }
}
