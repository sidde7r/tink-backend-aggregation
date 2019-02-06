package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;

public class AktiaAutoAuthenticator implements AutoAuthenticator {
    private final AktiaAuthenticationFlow aktiaAuthenticationFlow;

    public AktiaAutoAuthenticator(AktiaApiClient apiClient, EncapClient encapClient) {
        this.aktiaAuthenticationFlow = new AktiaAuthenticationFlow(apiClient, encapClient);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException, AuthorizationException {
        aktiaAuthenticationFlow.authenticate();
    }
}
