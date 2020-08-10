package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.PostbankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.PostbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;

public final class PostbankAgent extends DeutscheBankAgent {
    private static final DeutscheMarketConfiguration POSTBANK_CONFIGURATION =
            new DeutscheMarketConfiguration("https://xs2a.db.com/ais/DE/Postbank", "DE_ONLB_POBA");

    @Inject
    public PostbankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(String redirectUrl) {
        return new PostbankApiClient(client, sessionStorage, redirectUrl, POSTBANK_CONFIGURATION);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final PostbankAuthenticator postbankAuthenticator =
                new PostbankAuthenticator(
                        (PostbankApiClient) apiClient,
                        sessionStorage,
                        credentials.getField(CredentialKeys.IBAN));

        PostbankAuthenticationController postbankAuthenticationController =
                new PostbankAuthenticationController(
                        catalog, supplementalInformationHelper, postbankAuthenticator);

        return new AutoAuthenticationController(
                request, context, postbankAuthenticationController, postbankAuthenticator);
    }
}
