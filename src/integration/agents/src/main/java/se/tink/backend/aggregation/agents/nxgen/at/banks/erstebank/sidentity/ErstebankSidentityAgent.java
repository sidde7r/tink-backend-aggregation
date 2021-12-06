package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.ErsteBankPasswordAgent;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.ErstebankSidentityAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class ErstebankSidentityAgent extends ErsteBankPasswordAgent {

    private final ErsteBankApiClient ersteBankApiClient;

    @Inject
    public ErstebankSidentityAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.ersteBankApiClient = new ErsteBankApiClient(client, persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new ErstebankSidentityAuthenticator(
                ersteBankApiClient, supplementalInformationHelper);
    }
}
