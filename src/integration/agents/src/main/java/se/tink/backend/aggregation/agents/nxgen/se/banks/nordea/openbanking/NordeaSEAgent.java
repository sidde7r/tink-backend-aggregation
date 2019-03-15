package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking.transactionalaccount.NordeaSEAccountParser;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking.transactionalaccount.NordeaSETransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.NordeaBankIDAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaTransactionParser;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaSEAgent extends NordeaBaseAgent {

    public NordeaSEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        getNordeaPersistentStorage().setCountry(NordeaSEConstants.COUNTRY);
    }

    @Override
    protected Authenticator constructAuthenticator(NordeaBaseApiClient apiClient) {
        return new BankIdAuthenticationController<>(supplementalRequester,
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
