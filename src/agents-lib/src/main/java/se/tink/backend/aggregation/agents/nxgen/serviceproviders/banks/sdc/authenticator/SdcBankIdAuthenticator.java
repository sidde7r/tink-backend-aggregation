package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.agents.rpc.Credentials;

public class SdcBankIdAuthenticator implements BankIdAuthenticator<String> {

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
        bankClient.initSession();

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
