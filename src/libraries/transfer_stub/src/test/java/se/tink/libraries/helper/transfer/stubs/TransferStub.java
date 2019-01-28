package se.tink.libraries.helper.transfer.stubs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.transfer.enums.TransferPayloadType;

public class TransferStub {
    public static BankTransferBuilder bankTransfer() {
        return new BankTransferBuilder();
    }

    public static PaymentBuilder payment() {
        return new PaymentBuilder();
    }

    public static EInvoiceBuilder eInvoice() {
        return new EInvoiceBuilder();
    }

    public static class EInvoiceBuilder extends Builder<EInvoiceBuilder> {
        private static final ObjectMapper MAPPER = new ObjectMapper();

        private EInvoiceBuilder() {
            super(TransferType.EINVOICE);
        }

        public EInvoiceBuilder withOriginalTransfer(Transfer originalTransfer) {
            try {
                transfer.addPayload(TransferPayloadType.ORIGINAL_TRANSFER, MAPPER.writeValueAsString(originalTransfer));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return this;
        }

        public EInvoiceBuilder createUpdateTransferFromOriginal(Transfer originalTransfer) {
            from(originalTransfer.getSource())
                    .to(originalTransfer.getDestination())
                    .withAmount(originalTransfer.getAmount())
                    .withDestinationMessage(originalTransfer.getDestinationMessage())
                    .withSourceMessage(originalTransfer.getSourceMessage())
                    .withDueDate(originalTransfer.getDueDate());

            withOriginalTransfer(originalTransfer);
            return this;
        }
    }

    public static class PaymentBuilder extends Builder<PaymentBuilder> {
        private PaymentBuilder() {
            super(TransferType.PAYMENT);
        }
    }

    public static class BankTransferBuilder extends Builder<BankTransferBuilder> {
        private BankTransferBuilder() {
            super(TransferType.BANK_TRANSFER);
        }
    }

    private static class Builder<T extends Builder> {
        protected Transfer transfer;

        private Builder(TransferType type) {
            transfer = new Transfer();
            transfer.setType(type);
        }

        public T from(AccountIdentifier sourceIdentifier) {
            transfer.setSource(sourceIdentifier);
            return (T)this;
        }

        public T to(AccountIdentifier sourceIdentifier) {
            transfer.setDestination(sourceIdentifier);
            return (T)this;
        }

        public T withAmountInSEK(double value) {
            transfer.setAmount(Amount.inSEK(value));
            return (T)this;
        }

        public T withAmount(Amount amount) {
            transfer.setAmount(amount);
            return (T)this;
        }

        public T withSourceMessage(String sourceMessage) {
            transfer.setSourceMessage(sourceMessage);
            return (T)this;
        }

        public T withDestinationMessage(String destinationMessage) {
            transfer.setDestinationMessage(destinationMessage);
            return (T)this;
        }

        public T withMessage(String message) {
            withSourceMessage(message);
            withDestinationMessage(message);
            return (T)this;
        }

        public T withNoMessage() {
            withMessage(null);
            return (T)this;
        }

        public T withTooLongMessage() {
            withDestinationMessage("DstMsgLong Too long message I want to have since it is way too long for to read on the transaction list that hopefully either throws or gets cut off by the agent using some transfer message formatting");
            withSourceMessage("SrcMsgLong Too long message I want to have since it is way too long for to read on the transaction list that hopefully either throws or gets cut off by the agent using some transfer message formatting");
            return (T)this;
        }

        public T withDueDate(Date dueDate) {
            transfer.setDueDate(dueDate);
            return (T)this;
        }

        public Transfer build() {
            return transfer;
        }
    }
}
