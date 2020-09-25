package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.security.cert.CertificateException;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class UniversoAgent extends Xs2aDevelopersTransactionalAgent {

    private Xs2aDevelopersAuthenticator authenticator;

    @Inject
    public UniversoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api.psd2.universo.pt");
        authenticator =
                new Xs2aDevelopersAuthenticator(apiClient, persistentStorage, configuration);
    }

    @Override
    protected Xs2aDevelopersApiClient getApiClient() {
        return new UniversoApiClient(
                client, persistentStorage, (UniversoProviderConfiguration) configuration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState,
                        request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected UniversoProviderConfiguration getConfiguration(String baseUrl) {
        AgentConfiguration<UniversoConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(UniversoConfiguration.class);
        String organizationIdentifier;
        try {
            organizationIdentifier =
                    CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
        } catch (CertificateException e) {
            throw new IllegalStateException("Could not extract organization identifier!", e);
        }
        String redirectUrl = agentConfiguration.getRedirectUrl();
        UniversoConfiguration universoConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        return new UniversoProviderConfiguration(
                organizationIdentifier, baseUrl, redirectUrl, universoConfiguration.getApiKey());
    }
}
