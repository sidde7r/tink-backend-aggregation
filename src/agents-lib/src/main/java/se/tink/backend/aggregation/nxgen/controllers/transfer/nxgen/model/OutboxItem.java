package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import com.google.common.base.Preconditions;
import se.tink.libraries.amount.Amount;

public class OutboxItem {
    private TransferSource source;
    private TransferEntity destination;
    private Amount amount;

    private OutboxItem() {

    }

    public TransferSource getSource() {
        return source;
    }

    public TransferEntity getDestination() {
        return destination;
    }

    public Amount getAmount() {
        return amount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TransferSource source;
        private TransferEntity destination;
        private Amount amount;

        public Builder withSource(TransferSource source) {
            this.source = source;
            return this;
        }

        public Builder withDestination(TransferEntity destination) {
            this.destination = destination;
            return this;
        }

        public Builder withAmount(Amount amount) {
            this.amount = amount;
            return this;
        }

        public OutboxItem build() {
            Preconditions.checkNotNull(source, "Source must not be null.");
            Preconditions.checkNotNull(destination, "Destination must not be null.");
            Preconditions.checkNotNull(amount, "Amount must not be null.");

            OutboxItem command = new OutboxItem();
            command.source = source;
            command.destination = destination;
            command.amount = amount;

            return command;
        }
    }

}
