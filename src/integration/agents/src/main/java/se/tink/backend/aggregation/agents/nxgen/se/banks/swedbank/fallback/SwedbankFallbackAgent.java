package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Locale;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.SwedbankFallbackConstants.Filters;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.configuration.SwedbankPsd2Configuration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter.SwedbankFallbackBadGatewayRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter.SwedbankFallbackHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, PAYMENTS, SAVINGS_ACCOUNTS, IDENTITY_DATA, TRANSFERS})
public final class SwedbankFallbackAgent extends SwedbankAbstractAgent {

    @Inject
    protected SwedbankFallbackAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(
                componentProvider,
                new SwedbankConfiguration(
                        SwedbankFallbackConstants.PROFILE_PARAMETERS.get(
                                componentProvider
                                        .getCredentialsRequest()
                                        .getProvider()
                                        .getPayload()),
                        SwedbankFallbackConstants.HOST,
                        true),
                new SwedbankFallbackApiClientProvider(agentsServiceConfiguration),
                new SwedbankDateUtils(ZoneId.of("Europe/Stockholm"), new Locale("sv", "SE")));

        final AgentConfiguration agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(SwedbankPsd2Configuration.class);

        final SwedbankPsd2Configuration psd2Configuration =
                (SwedbankPsd2Configuration) agentConfiguration.getProviderSpecificConfiguration();

        final String qSealc = agentConfiguration.getQsealc();

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        client.addFilter(
                new SwedbankFallbackHttpFilter(
                        componentProvider.getRandomValueGenerator(),
                        psd2Configuration,
                        agentsServiceConfiguration,
                        getEidasIdentity(),
                        qSealc));
        client.addFilter(
                new SwedbankFallbackBadGatewayRetryFilter(
                        Filters.NUMBER_OF_RETRIES, Filters.MS_TO_WAIT));
    }
}
