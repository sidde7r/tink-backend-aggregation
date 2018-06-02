package se.tink.backend.system.workers.processor.other;

import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.core.Transaction;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

public class SendTransactionsToFirehoseCommand implements TransactionProcessorCommand {
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final TransactionProcessorContext context;

    public SendTransactionsToFirehoseCommand(
            TransactionProcessorContext context,
            FirehoseQueueProducer firehoseQueueProducer) {
        this.context = context;
        this.firehoseQueueProducer = firehoseQueueProducer;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        send(getTransactionsToCreate(), FirehoseMessage.Type.CREATE);
        send(getTransactionsToUpdate(), FirehoseMessage.Type.UPDATE);
        send(context.getTransactionsToDelete(), FirehoseMessage.Type.DELETE);
    }

    private List<Transaction> getTransactionsToCreate() {
        return context.getTransactionsToSave().values().stream()
                .filter(transaction -> !context.getTransactionsToUpdateList().contains(transaction.getId()))
                .collect(Collectors.toList());
    }

    private List<Transaction> getTransactionsToUpdate() {
        return context.getTransactionsToUpdateList().stream()
                .map(id -> Optional.ofNullable(context.getTransactionsToSave().get(id))
                        .orElseGet(() -> context.getUserData().getInStoreTransaction(id)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void send(List<Transaction> transactions, FirehoseMessage.Type type) {
        if (transactions.isEmpty()) {
            return;
        }
        firehoseQueueProducer.sendTransactionsMessage(transactions.get(0).getUserId(), type, transactions);
    }

    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("firehoseQueueProducer", firehoseQueueProducer)
                .toString();
    }
}
