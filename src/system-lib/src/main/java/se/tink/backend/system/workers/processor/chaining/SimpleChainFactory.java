package se.tink.backend.system.workers.processor.chaining;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

public class SimpleChainFactory implements ChainFactory {

    private final Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> builder;

    public SimpleChainFactory(
            Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> builder) {
        this.builder = builder;
    }

    @Override
    public ImmutableList<TransactionProcessorCommand> build(TransactionProcessorContext context) {
        return builder.apply(context);
    }

    @Override
    public String getUniqueIdentifier() {
        return "simple-chain";
    }

    @Override
    public void close() {
        // Intentionally left empty
    }
}
