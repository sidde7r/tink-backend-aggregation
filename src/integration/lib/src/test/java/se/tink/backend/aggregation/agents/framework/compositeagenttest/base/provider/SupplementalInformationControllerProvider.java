package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Map;
import javax.annotation.Nullable;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module.SupplementalInformationCallbackData;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public final class SupplementalInformationControllerProvider
        implements Provider<SupplementalInformationController> {

    private final AgentContext context;
    private final Credentials credential;
    private final Map<String, String> callbackData;

    @Inject
    public SupplementalInformationControllerProvider(
            AgentContext context,
            Credentials credential,
            @Nullable @SupplementalInformationCallbackData Map<String, String> callbackData) {
        this.context = context;
        this.credential = credential;
        this.callbackData = callbackData;
    }

    @Override
    public SupplementalInformationController get() {
        return new MockSupplementalInformationController(callbackData);
    }
}
