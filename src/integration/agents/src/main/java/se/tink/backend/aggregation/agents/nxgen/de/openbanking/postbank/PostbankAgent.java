package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.PostbankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.PostbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class PostbankAgent extends DeutscheBankAgent {
    private static final DeutscheMarketConfiguration POSTBANK_CONFIGURATION =
            new DeutscheMarketConfiguration("https://xs2a.db.com/ais/DE/Postbank", "DE_ONLB_POBA");

    @Inject
    public PostbankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(DeutscheHeaderValues headerValues) {
        return new PostbankApiClient(
                client, persistentStorage, headerValues, POSTBANK_CONFIGURATION);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final PostbankAuthenticator postbankAuthenticator =
                new PostbankAuthenticator((PostbankApiClient) apiClient, persistentStorage);

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog,
                        supplementalInformationHelper,
                        supplementalRequester,
                        postbankAuthenticator);

        return new AutoAuthenticationController(
                request, context, postbankAuthenticationController, postbankAuthenticator);
    }
}
