package se.tink.backend.aggregation.agents.framework.compositeagenttest.base;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.logmasker.LogMasker;

@Slf4j
public final class CompositeAgentTest {

    private final NewAgentTestContext context;
    private final Set<CompositeAgentTestCommand> commandSequence;
    private final boolean httpDebugTraceEnabled;

    @Inject
    private CompositeAgentTest(
            NewAgentTestContext context,
            Set<CompositeAgentTestCommand> commandSequence,
            @Named("httpDebugTraceEnabled") boolean httpDebugTraceEnabled) {
        this.context = context;
        /* Guice guarantees that injected set has deterministic iteration order
        which is consistent with the binding order. */
        this.commandSequence = commandSequence;
        this.httpDebugTraceEnabled = httpDebugTraceEnabled;
    }

    public void executeCommands() throws Exception {

        try {
            for (CompositeAgentTestCommand command : commandSequence) {
                command.execute();
            }
        } catch (Exception e) {
            log.error(printHttpDebugTrace());
            throw e;
        }

        if (httpDebugTraceEnabled) {
            log.info(printHttpDebugTrace());
        }
    }

    public NewAgentTestContext getContext() {
        return context;
    }

    private String printHttpDebugTrace() {
        final LogMasker logMasker = context.getLogMasker();
        final String maskedLog = logMasker.mask(context.getLogOutputStream().toString());

        return "\n" + "===== HTTP DEBUG TRACE LOG =====\n" + maskedLog + "\n";
    }
}
