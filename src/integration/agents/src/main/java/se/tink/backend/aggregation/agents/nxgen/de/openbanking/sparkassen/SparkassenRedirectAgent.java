package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.EnumSet;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.sparkassen.SparkasenConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.sparkassen.SparkassenScope;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.SparkassenRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.SparkassenRedirectHelper;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;

@AgentCapabilities({CHECKING_ACCOUNTS})
public class SparkassenRedirectAgent extends SparkassenAgent {

    private final LogMasker logMasker;

    @Inject
    public SparkassenRedirectAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.logMasker = componentProvider.getContext().getLogMasker();
    }

    @Override
    protected SparkassenApiClient constructApiClient() {
        String bankCode = provider.getPayload();
        AgentConfiguration<SparkassenConfiguration> agentConfiguration = getAgentConfiguration();
        String redirectUrl = agentConfiguration.getRedirectUrl();
        SparkassenHeaderValues headerValues =
                SparkassenHeaderValues.forRedirect(
                        bankCode,
                        redirectUrl,
                        request.getUserAvailability().getOriginatingUserIpOrDefault());
        return new SparkassenApiClient(
                client,
                headerValues,
                sparkassenStorage,
                randomValueGenerator,
                null,
                new SparkasenConsentGenerator(
                        componentProvider.getCredentialsRequest(),
                        localDateTimeSource,
                        EnumSet.allOf(SparkassenScope.class)));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        String clientId = getClientId();
        this.logMasker.addNewSensitiveValuesToMasker(Collections.singleton(clientId));
        SparkassenRedirectHelper helper =
                new SparkassenRedirectHelper(
                        randomValueGenerator, sparkassenStorage, apiClient, clientId, logMasker);
        SparkassenRedirectAuthenticator authenticator =
                new SparkassenRedirectAuthenticator(
                        new OAuth2AuthenticationController(
                                persistentStorage,
                                supplementalInformationHelper,
                                helper,
                                credentials,
                                strongAuthenticationState),
                        apiClient,
                        sparkassenStorage,
                        credentials);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        authenticator, supplementalInformationHelper),
                authenticator);
    }

    private String getClientId() {
        AgentConfiguration<SparkassenConfiguration> agentConfiguration = getAgentConfiguration();
        String organizationIdentifier;
        try {
            organizationIdentifier =
                    CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
        } catch (CertificateException e) {
            throw new IllegalStateException("Could not extract organization identifier!", e);
        }
        return organizationIdentifier;
    }

    private AgentConfiguration<SparkassenConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(SparkassenConfiguration.class);
    }
}
