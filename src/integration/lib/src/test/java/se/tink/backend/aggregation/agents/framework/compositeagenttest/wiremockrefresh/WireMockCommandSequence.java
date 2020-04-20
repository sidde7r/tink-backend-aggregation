package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Iterator;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommandSequence;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.command.LoginCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.command.RefreshCommand;

public final class WireMockCommandSequence implements CompositeAgentTestCommandSequence {

    private final List<CompositeAgentTestCommand> commandSequence;

    @Inject
    private WireMockCommandSequence(LoginCommand loginCommand, RefreshCommand refreshCommand) {
        this.commandSequence = ImmutableList.of(loginCommand, refreshCommand);
    }

    @Override
    public Iterator<CompositeAgentTestCommand> iterator() {
        return commandSequence.listIterator();
    }
}
