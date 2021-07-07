package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.PIS_SEPA_RECURRING_PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.SEPA_CREDIT_TRANSFER;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorageProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(capabilities = {SEPA_CREDIT_TRANSFER, PIS_SEPA_RECURRING_PAYMENTS})
public final class BpmAgent extends CbiGlobeAgent {

    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    public BpmAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
        this.localDateTimeSource = agentComponentProvider.getLocalDateTimeSource();
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new BpmApiClient(
                client,
                new CbiStorageProvider(persistentStorage, sessionStorage, temporaryStorage),
                requestManual,
                getProviderConfiguration(),
                psuIpAddress,
                randomValueGenerator,
                localDateTimeSource);
    }
}
