package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.nordmidipyrenees;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = CreditAgricoleBaseModule.class)
@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LIST_BENEFICIARIES,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    TRANSFERS,
    CREDIT_CARDS
})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public final class CreditagricoleNordmidipyreneesAgent extends CreditAgricoleBaseAgent {

    @Inject
    public CreditagricoleNordmidipyreneesAgent(
            AgentComponentProvider componentProvider,
            QSealSignatureProvider qSealSignatureProvider) {
        super(componentProvider, qSealSignatureProvider);
    }
}
