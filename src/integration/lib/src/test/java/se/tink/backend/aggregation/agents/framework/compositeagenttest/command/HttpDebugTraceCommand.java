package se.tink.backend.aggregation.agents.framework.compositeagenttest.command;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.logmasker.LogMasker;

public class HttpDebugTraceCommand implements CompositeAgentTestCommand {

    private final NewAgentTestContext context;

    @Inject
    private HttpDebugTraceCommand(NewAgentTestContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        final LogMasker logMasker = context.getLogMasker();
        final String maskedLog = logMasker.mask(context.getLogOutputStream().toString());

        System.out.println();
        System.out.println("===== HTTP DEBUG TRACE LOG =====");
        System.out.println(maskedLog);
        System.out.println();
    }
}
