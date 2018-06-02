package se.tink.backend.system.workers.processor.chaining;

import com.google.common.collect.ImmutableList;
import java.io.Closeable;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

public interface ChainFactory extends Closeable {
    ImmutableList<TransactionProcessorCommand> build(TransactionProcessorContext context);

    String getUniqueIdentifier();
}
