package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.openbanking;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.openbanking.transactionalaccount.NordeaFIAccountParser;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.openbanking.transactionalaccount.NordeaFITransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.NordeaOauthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaTransactionParser;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class NordeaFIAgent extends NordeaBaseAgent {

    public NordeaFIAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        getNordeaPersistentStorage().setCountry(NordeaFIConstants.COUNTRY);
    }

    @Override
    protected Authenticator constructAuthenticator(NordeaBaseApiClient apiClient) {

        OAuth2Authenticator oauthAuthenticator = new NordeaOauthAuthenticator(apiClient,
                new NordeaSessionStorage(sessionStorage),
                getNordeaPersistentStorage());

        return OAuth2AuthenticationFlow.create(request, context,
                persistentStorage, supplementalInformationController,
                oauthAuthenticator);
    }

    @Override
    protected NordeaAccountParser createAccountParser() {
        return new NordeaFIAccountParser();
    }

    @Override
    protected NordeaTransactionParser createTransactionParser() {
        return new NordeaFITransactionParser();
    }
}
