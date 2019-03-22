package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.ErsteBankPasswordAgent;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.ErstebankSidentityAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ErstebankSidentityAgent extends ErsteBankPasswordAgent {

    private final ErsteBankApiClient ersteBankApiClient;

    public ErstebankSidentityAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.ersteBankApiClient = new ErsteBankApiClient(client, persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new ErstebankSidentityAuthenticator(
                ersteBankApiClient, supplementalInformationHelper);
    }
}
