package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.bankid.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.bankid.SessionBodyEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IcaBankenBankIdAuthenticator implements BankIdAuthenticator<String> {

    private final IcaBankenApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IcaBankenBankIdAuthenticator(IcaBankenApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String init(String ssn) {

        BankIdResponse response = apiClient.initBankId(ssn);
        SessionBodyEntity responseBody = response.getBody();

        return response.getBody().getRequestId();
    }

    @Override
    public BankIdStatus collect(String reference) throws BankIdException {
        BankIdResponse response = apiClient.authenticate(reference);

        SessionBodyEntity sessionBody = response.getBody();
        if (Objects.requireNonNull(sessionBody).getBankIdStatus().equals(BankIdStatus.DONE)) {
            // Authentication was successful. Save the session id.
            sessionStorage.put(IcaBankenConstants.IdTags.SESSION_ID_TAG, sessionBody.getSessionId());
        }

        return response.getBody().getBankIdStatus();
    }
}
