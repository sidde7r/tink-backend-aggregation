package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.saastopankki;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkAgent;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class SpAgent extends SamlinkAgent {

    public SpAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider, qsealcSigner);
    }
}
