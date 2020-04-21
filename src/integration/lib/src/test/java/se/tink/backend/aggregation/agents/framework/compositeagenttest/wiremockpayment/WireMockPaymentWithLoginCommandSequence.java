package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Iterator;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommandSequence;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.command.LoginCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;

/** Command sequence for performing a payment that will try to login before executing payment. */
public final class WireMockPaymentWithLoginCommandSequence
        implements CompositeAgentTestCommandSequence {

    private final List<CompositeAgentTestCommand> commandSequence;

    @Inject
    private WireMockPaymentWithLoginCommandSequence(
            LoginCommand loginCommand, PaymentCommand paymentCommand) {
        this.commandSequence = ImmutableList.of(loginCommand, paymentCommand);
    }

    @Override
    public Iterator<CompositeAgentTestCommand> iterator() {
        return commandSequence.listIterator();
    }
}
