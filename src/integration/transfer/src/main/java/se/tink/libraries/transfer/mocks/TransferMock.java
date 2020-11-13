package se.tink.libraries.transfer.mocks;

import java.util.Date;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferMock {
    public static BankTransferBuilder bankTransfer() {
        return new BankTransferBuilder();
    }

    public static PaymentBuilder payment() {
        return new PaymentBuilder();
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
            return (T) this;
        }

        public T to(AccountIdentifier sourceIdentifier) {
            transfer.setDestination(sourceIdentifier);
            return (T) this;
        }

        public T withAmountInSEK(double value) {
            transfer.setAmount(Amount.inSEK(value));
            return (T) this;
        }

        public T withAmount(Amount amount) {
            transfer.setAmount(amount);
            return (T) this;
        }

        public T withSourceMessage(String sourceMessage) {
            transfer.setSourceMessage(sourceMessage);
            return (T) this;
        }

        public T withRemittanceInformation(RemittanceInformation remittanceInformation) {
            transfer.setRemittanceInformation(remittanceInformation);
            return (T) this;
        }

        public T withMessage(String message) {
            withSourceMessage(message);
            withRemittanceInformation(createAndGetRemittanceInformation(null, message));
            return (T) this;
        }

        public T withNoMessage() {
            withMessage(null);
            return (T) this;
        }

        public T withTooLongMessage() {
            withRemittanceInformation(
                    createAndGetRemittanceInformation(
                            null,
                            "DstMsgLong Too long message I want to have since it is way too long for to read on the transaction list that hopefully either throws or gets cut off by the agent using some transfer message formatting"));
            withSourceMessage(
                    "SrcMsgLong Too long message I want to have since it is way too long for to read on the transaction list that hopefully either throws or gets cut off by the agent using some transfer message formatting");
            return (T) this;
        }

        public T withDueDate(Date dueDate) {
            transfer.setDueDate(dueDate);
            return (T) this;
        }

        private static RemittanceInformation createAndGetRemittanceInformation(
                RemittanceInformationType type, String value) {
            RemittanceInformation remittanceInformation = new RemittanceInformation();
            remittanceInformation.setType(type);
            remittanceInformation.setValue(value);
            return remittanceInformation;
        }

        public Transfer build() {
            return transfer;
        }
    }
}
