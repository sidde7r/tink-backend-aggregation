package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.DeutscheBankMultifactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class DeutscheBankBEAgent extends DeutscheBankAgent {
    private static final DeutscheMarketConfiguration DEUTSCHE_BE_CONFIGURATION =
            new DeutscheMarketConfiguration(
                    "https://xs2a.db.com/{" + Parameters.SERVICE_KEY + "}/BE/DB", "BE_ONLB_DB");

    @Inject
    public DeutscheBankBEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(DeutscheHeaderValues headerValues) {
        return new DeutscheBankApiClient(
                client, persistentStorage, headerValues, DEUTSCHE_BE_CONFIGURATION);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final DeutscheBankMultifactorAuthenticator deutscheBankAuthenticatorController =
                new DeutscheBankMultifactorAuthenticator(
                        apiClient,
                        persistentStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);

        return new AutoAuthenticationController(
                request,
                context,
                deutscheBankAuthenticatorController,
                deutscheBankAuthenticatorController);
    }
}
