package se.tink.backend.aggregation.agents.framework.compositeagenttest.base;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;

public final class CompositeAgentTest {

    private final NewAgentTestContext context;
    private final CompositeAgentTestCommandSequence commandSequence;

    @Inject
    private CompositeAgentTest(
            NewAgentTestContext context, CompositeAgentTestCommandSequence commandSequence) {
        this.context = context;
        this.commandSequence = commandSequence;
    }

    public void execute() throws Exception {

        for (CompositeAgentTestCommand command : commandSequence) {
            command.execute();
        }
    }

    public NewAgentTestContext getContext() {
        return context;
    }
}
