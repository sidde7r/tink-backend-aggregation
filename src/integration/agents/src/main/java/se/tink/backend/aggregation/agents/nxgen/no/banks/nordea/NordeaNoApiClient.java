package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.collect.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.collect.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.init.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.init.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class NordeaNoApiClient extends NordeaV17ApiClient {
    public NordeaNoApiClient(TinkHttpClient client, Credentials credentials, String marketCode) {
        super(client, credentials, marketCode);
    }

    public BankIdInitResponse initiateBankId(String dob, String mobileNumber)
            throws AuthenticationException, AuthorizationException {
        return authRequest(new BankIdInitRequest(dob, mobileNumber), BankIdInitResponse.class);
    }

    public BankIdCollectResponse pollBankId(String sessionId)
            throws AuthenticationException, AuthorizationException {
        return authRequest(new BankIdCollectRequest(sessionId), BankIdCollectResponse.class);
    }
}
