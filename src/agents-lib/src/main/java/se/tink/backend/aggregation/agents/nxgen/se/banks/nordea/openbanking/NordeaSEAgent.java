package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking.transactionalaccount.NordeaSEAccountParser;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking.transactionalaccount.NordeaSETransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.NordeaBankIDAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.NordeaTransactionParser;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class NordeaSEAgent extends NordeaBaseAgent {

    public NordeaSEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        getNordeaPersistentStorage().setCountry(NordeaSEConstants.COUNTRY);
    }

    @Override
    protected Authenticator constructAuthenticator(NordeaBaseApiClient apiClient) {
        return new BankIdAuthenticationController<>(context,
                new NordeaBankIDAuthenticator(apiClient,
                        new NordeaSessionStorage(sessionStorage),
                        getNordeaPersistentStorage()));
    }

    @Override
    protected NordeaAccountParser createAccountParser() {
        return new NordeaSEAccountParser();
    }

    @Override
    protected NordeaTransactionParser createTransactionParser() {
        return new NordeaSETransactionParser();
    }
}
