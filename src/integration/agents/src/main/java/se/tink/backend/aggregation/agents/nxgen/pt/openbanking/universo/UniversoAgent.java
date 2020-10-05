package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class UniversoAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public UniversoAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider, "https://api.sandbox.psd2.universo.pt");
        client.addFilter(
                new UniversoSigningFilter(
                        (UniversoProviderConfiguration) configuration, qsealcSigner));
    }

    @Override
    @SneakyThrows
    protected UniversoProviderConfiguration getConfiguration(String baseUrl) {
        AgentConfiguration<UniversoConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(UniversoConfiguration.class);
        String organizationIdentifier =
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
        String redirectUrl = agentConfiguration.getRedirectUrl();
        UniversoConfiguration universoConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();

        return new UniversoProviderConfiguration(
                organizationIdentifier,
                baseUrl,
                redirectUrl,
                universoConfiguration.getApiKey(),
                universoConfiguration.getKeyId(),
                agentConfiguration.getQsealc());
    }
}
