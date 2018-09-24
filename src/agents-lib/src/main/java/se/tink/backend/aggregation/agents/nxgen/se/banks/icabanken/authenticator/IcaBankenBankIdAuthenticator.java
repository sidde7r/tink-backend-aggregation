package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator;

import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import org.apache.http.HttpStatus;

public class IcaBankenBankIdAuthenticator implements BankIdAuthenticator<String> {
    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage icaBankenSessionStorage;

    public IcaBankenBankIdAuthenticator(IcaBankenApiClient apiClient, IcaBankenSessionStorage icaBankenSessionStorage) {
        this.apiClient = apiClient;
        this.icaBankenSessionStorage = icaBankenSessionStorage;
    }

    @Override
    public String init(String ssn) throws BankIdException {
        try {
            return apiClient.initBankId(ssn);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }

            throw e;
        }
    }

    @Override
    public BankIdStatus collect(String reference) throws AuthenticationException {

        BankIdResponse response = getPollResponse(reference);
        BankIdStatus bankIdStatus = response.getBankIdStatus();

        if (bankIdStatus == BankIdStatus.DONE) {
            icaBankenSessionStorage.saveSessionId(response.getBody().getSessionId());
        }

        return bankIdStatus;
    }

    private BankIdResponse getPollResponse(String reference) throws BankIdException {
        try {
            return apiClient.pollBankId(reference);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.INTERRUPTED.exception();
            }

            throw e;
        }
    }
}
