package se.tink.backend.aggregation.agents.framework.compositeagenttest.base;

import com.google.inject.Inject;
import java.util.Set;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;

public final class CompositeAgentTest {

    private final NewAgentTestContext context;
    private final Iterable<CompositeAgentTestCommand> commandSequence;

    @Inject
    private CompositeAgentTest(
            NewAgentTestContext context, Set<CompositeAgentTestCommand> commandSequence) {
        this.context = context;
        /* Guice guarantees that injected set has deterministic iteration order
        which is consistent with the binding order. */
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
