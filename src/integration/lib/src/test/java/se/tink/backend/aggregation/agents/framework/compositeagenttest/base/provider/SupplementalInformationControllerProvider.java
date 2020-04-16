package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;

public final class SupplementalInformationControllerProvider
        implements Provider<SupplementalInformationController> {

    private final AgentContext context;
    private final Credentials credential;

    @Inject
    public SupplementalInformationControllerProvider(AgentContext context, Credentials credential) {
        this.context = context;
        this.credential = credential;
    }

    @Override
    public SupplementalInformationController get() {
        return new SupplementalInformationControllerImpl(context, credential);
    }
}
