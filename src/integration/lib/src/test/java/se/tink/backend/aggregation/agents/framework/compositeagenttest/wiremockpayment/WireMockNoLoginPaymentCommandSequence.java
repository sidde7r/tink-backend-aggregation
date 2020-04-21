package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Iterator;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommandSequence;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;

/**
 * Command sequence for performing a payment that does not try to login before executing payment.
 */
public final class WireMockNoLoginPaymentCommandSequence
        implements CompositeAgentTestCommandSequence {

    private final List<CompositeAgentTestCommand> commandSequence;

    @Inject
    private WireMockNoLoginPaymentCommandSequence(PaymentCommand paymentCommand) {
        this.commandSequence = ImmutableList.of(paymentCommand);
    }

    @Override
    public Iterator<CompositeAgentTestCommand> iterator() {
        return commandSequence.listIterator();
    }
}
