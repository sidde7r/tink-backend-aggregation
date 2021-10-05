package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.swedbank;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.swedbank.filters.AddBusinessFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankBaseAgent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({
    Capability.CHECKING_ACCOUNTS,
    Capability.SAVINGS_ACCOUNTS,
})
public class SwedbankSEBusinessAgent extends SwedbankBaseAgent {

    @Inject
    public SwedbankSEBusinessAgent(
            AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider, qsealcSigner, new SwedbankSEBusinessConfiguration());
        client.addFilter(new AddBusinessFilter(setBusinessId()));
    }

    private String setBusinessId() {
        String corporateFieldKey = returnKey(Key.CORPORATE_ID.getFieldKey());
        String usernameFieldKey = Key.USERNAME.getFieldKey();
        if (null != corporateFieldKey) {
            return corporateFieldKey.equals("")
                    ? returnKey(usernameFieldKey)
                    : returnKey(corporateFieldKey);
        }
        return returnKey(usernameFieldKey);
    }

    private String returnKey(String field) {
        return request.getCredentials().getField(field);
    }
}
