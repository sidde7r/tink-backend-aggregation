package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.finistere;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public class CreditagricoleFinistereAgent extends CreditAgricoleBaseAgent {

    @Inject
    public CreditagricoleFinistereAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider, qsealcSigner);
    }
}
