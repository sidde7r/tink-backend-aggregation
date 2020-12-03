package se.tink.backend.aggregation.agents.agentcapabilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.CreateBeneficiariesCapabilityExecutor;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

public class CapabilitiesExtractor {

    private static final List<Mapping> MAPPINGS =
            Arrays.asList(
                    new Mapping(RefreshCreditCardAccountsExecutor.class, Capability.CREDIT_CARDS),
                    new Mapping(RefreshIdentityDataExecutor.class, Capability.IDENTITY_DATA),
                    new Mapping(
                            RefreshCheckingAccountsExecutor.class, Capability.CHECKING_ACCOUNTS),
                    new Mapping(RefreshSavingsAccountsExecutor.class, Capability.SAVINGS_ACCOUNTS),
                    new Mapping(RefreshInvestmentAccountsExecutor.class, Capability.INVESTMENTS),
                    new Mapping(RefreshLoanAccountsExecutor.class, Capability.LOANS),
                    new Mapping(TransferExecutor.class, Capability.PAYMENTS),
                    new Mapping(TransferExecutorNxgen.class, Capability.PAYMENTS),
                    new Mapping(RefreshBeneficiariesExecutor.class, Capability.LIST_BENEFICIARIES),
                    new Mapping(
                            CreateBeneficiariesCapabilityExecutor.class,
                            Capability.CREATE_BENEFICIARIES),
                    new Mapping(TransferExecutor.class, Capability.PIS_SEPA),
                    new Mapping(TransferExecutorNxgen.class, Capability.PIS_SEPA),
                    new Mapping(TransferExecutor.class, Capability.PIS_SE_BG),
                    new Mapping(TransferExecutorNxgen.class, Capability.PIS_SE_BG),
                    new Mapping(TransferExecutor.class, Capability.PIS_SE_PG),
                    new Mapping(TransferExecutorNxgen.class, Capability.PIS_SE_PG),
                    new Mapping(TransferExecutor.class, Capability.PIS_SEPA_ICT),
                    new Mapping(TransferExecutorNxgen.class, Capability.PIS_SEPA_ICT),
                    new Mapping(TransferExecutor.class, Capability.PIS_UK_FASTER_PAYMENT),
                    new Mapping(TransferExecutorNxgen.class, Capability.PIS_UK_FASTER_PAYMENT));

    public static Set<Capability> readCapabilities(Class<? extends Agent> klass) {
        AgentCapabilities capabilities = klass.getAnnotation(AgentCapabilities.class);
        if (capabilities.generateFromImplementedExecutors()) {
            return readCapabilitiesFromInterfaces(klass);
        }
        return readCapabilitiesFromAnnotation(capabilities);
    }

    public static Set<Mapping> getMappingsFor(Capability capability) {
        return MAPPINGS.stream()
                .filter(mapping -> mapping.getCapability().equals(capability))
                .collect(Collectors.toSet());
    }

    private static Set<Capability> readCapabilitiesFromInterfaces(
            Class<? extends Agent> agentClass) {
        return MAPPINGS.stream()
                .filter(mapping -> mapping.canMap(agentClass))
                .map(Mapping::getCapability)
                .collect(Collectors.toSet());
    }

    private static Set<Capability> readCapabilitiesFromAnnotation(AgentCapabilities capabilities) {
        return new HashSet<>(Arrays.asList(capabilities.value()));
    }
}
