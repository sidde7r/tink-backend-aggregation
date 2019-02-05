package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import java.util.Objects;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.collect.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.init.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.authenticator.NordeaV17Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;

public class NordeaNoAuthenticator extends NordeaV17Authenticator<NordeaNoApiClient> implements BankIdAuthenticatorNO {
    private String sessionId;

    public NordeaNoAuthenticator(NordeaNoApiClient client) {
        super(client);
    }

    @Override
    public String init(String nationalId, String dob, String mobilenumber) throws AuthenticationException,
            AuthorizationException {
        BankIdInitResponse bankIdInitResponse = client.initiateBankId(dob, mobilenumber);

        if (bankIdInitResponse.isAlreadyInProgress()) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }

        sessionId = bankIdInitResponse.getSessionId();

        String authToken = bankIdInitResponse.getToken();
        client.setToken(authToken);

        return bankIdInitResponse.getMerchantReference();
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        BankIdCollectResponse bankIdPollResponse = client.pollBankId(sessionId);
        BankIdStatus status = bankIdPollResponse.getStatus();

        if (Objects.equals(status, BankIdStatus.DONE)) {
            client.setToken(bankIdPollResponse.getToken());
        }

        return status;
    }
}
