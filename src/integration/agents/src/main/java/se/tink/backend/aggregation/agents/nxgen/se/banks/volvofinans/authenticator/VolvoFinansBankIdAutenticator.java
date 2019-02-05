package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.authenticator.rpc.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class VolvoFinansBankIdAutenticator implements BankIdAuthenticator<String> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansBankIdAutenticator(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String init(String ssn) throws BankIdException {
        try {
            HttpResponse httpResponse = apiClient.loginBankIdInit(new InitBankIdRequest(ssn));
            String location = httpResponse.getHeaders().getFirst(VolvoFinansConstants.Headers.HEADER_LOCATION);
            return location.substring(location.lastIndexOf('/') + 1);
        } catch (HttpResponseException hre) {
            HttpResponse httpResponse = hre.getResponse();

            if (httpResponse.getStatus() == HttpStatus.SC_CONFLICT) {
                ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);

                if (errorResponse.isBankIdAlreadyInProgressError()) {
                    throw BankIdError.ALREADY_IN_PROGRESS.exception();
                }
            }

            throw hre;
        }
    }

    @Override
    public BankIdStatus collect(String identificationId) throws AuthenticationException {
        BankIdStatus bankIdStatus = apiClient.loginBankIdPoll(identificationId).getBankIdStatus();

        if (bankIdStatus.equals(BankIdStatus.DONE)) {
            try {
                apiClient.keepAlive();
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                    throw LoginError.NOT_CUSTOMER.exception();
                }
                throw e;
            }
        }

        return bankIdStatus;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }
}
