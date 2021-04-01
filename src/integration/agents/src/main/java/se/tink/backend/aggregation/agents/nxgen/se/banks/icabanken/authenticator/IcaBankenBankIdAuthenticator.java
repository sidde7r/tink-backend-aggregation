package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdAuthInitBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdAuthPollBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.SessionBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.entities.CustomerBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class IcaBankenBankIdAuthenticator implements BankIdAuthenticator<String> {
    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage icaBankenSessionStorage;

    private String ssn;
    private String autostarttoken;

    public IcaBankenBankIdAuthenticator(
            IcaBankenApiClient apiClient, IcaBankenSessionStorage icaBankenSessionStorage) {
        this.apiClient = apiClient;
        this.icaBankenSessionStorage = icaBankenSessionStorage;
    }

    @Override
    public String init(String ssn) throws AuthenticationException, AuthorizationException {
        this.ssn = ssn;
        try {
            final BankIdAuthInitBodyEntity response = apiClient.initBankId(ssn);
            autostarttoken = response.getAutoStartToken();
            return response.getOrderRef();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception(e);
            } else if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
            }

            throw e;
        }
    }

    @Override
    public BankIdStatus collect(String reference)
            throws AuthenticationException, AuthorizationException {

        BankIdAuthPollBodyEntity response = apiClient.pollBankId(reference);
        BankIdStatus bankIdStatus =
                IcaBankenConstants.BANKID_STATUS_MAPPER
                        .translate(response.getOrderStatus().toLowerCase())
                        .get();

        if (bankIdStatus == BankIdStatus.DONE) {
            final SessionBodyEntity authResponse = apiClient.authenticateBankId(reference);
            icaBankenSessionStorage.saveSessionId(authResponse.getSessionId());

            final CustomerBodyEntity customer = apiClient.fetchCustomer();
            if (!customer.isCustomer()
                    || customer.getEngagement() == null
                    || !customer.getEngagement().isHasActiveBank()) {
                throw LoginError.NOT_CUSTOMER.exception();
            }

            if (!customer.isUpdatedKDK()) {
                throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(
                        IcaBankenConstants.UserMessage.KNOW_YOUR_CUSTOMER.getKey());
            }

            // Policies are used to decide which endpoints are allowed to fetch
            icaBankenSessionStorage.savePolicies(apiClient.fetchPolicies().getOkPolicies());
        }

        return bankIdStatus;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autostarttoken);
    }

    @Override
    public String refreshAutostartToken()
            throws BankServiceException, AuthorizationException, AuthenticationException {
        return init(this.ssn);
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
