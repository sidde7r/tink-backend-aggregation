package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.UNEXPECTED_TYPE_ERROR;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.PaymentDateDependency;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ReturnValue;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.TransactionType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction.Builder;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
@Slf4j
@Getter
public class ConfirmedTransactionEntity extends AbstractExecutorTransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private Date bookedDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private Date date;

    private PaymentEntity payment;
    private String noteToSender;
    private List<String> scopes;

    @JsonIgnore
    public Optional<UpcomingTransaction> toTinkUpcomingTransaction(
            AccountIdentifier sourceAccount) {
        double parsedAmount = StringUtils.parseAmountEU(this.amount);
        if (Strings.isNullOrEmpty(currencyCode) || !Double.isFinite(parsedAmount)) {
            return Optional.empty();
        }

        ExactCurrencyAmount exactCurrencyAmount =
                ExactCurrencyAmount.of(parsedAmount, currencyCode);

        // negate amount when presented as upcoming transaction
        exactCurrencyAmount = exactCurrencyAmount.negate();

        if (date == null || shouldSkipPayment()) {
            return Optional.empty();
        }

        UpcomingTransaction.Builder upcomingTransactionBuilder =
                createUpcomingTransactionBuilder(exactCurrencyAmount);

        Optional<Transfer> upcomingTransfer = getUpcomingTransfer(sourceAccount);

        if (TransactionType.PAYMENT.equalsIgnoreCase(type) && upcomingTransfer.isPresent()) {
            upcomingTransactionBuilder.setUpcomingTransfer(upcomingTransfer.get());
        }

        return Optional.of(upcomingTransactionBuilder.build());
    }

    private Builder createUpcomingTransactionBuilder(ExactCurrencyAmount exactCurrencyAmount) {
        return UpcomingTransaction.builder()
                .setAmount(exactCurrencyAmount)
                .setDate(date)
                .setDescription(getSourceMessage());
    }

    // Skip if the transactions are already in the normal transaction list.
    private boolean shouldSkipPayment() {
        if (isTypeSame(TransactionType.TRANSFER)) {
            return PaymentDateDependency.DIRECT.equalsIgnoreCase(transfer.getDateDependency());
        } else if (isTypeSame(TransactionType.PAYMENT)) {
            return PaymentDateDependency.DIRECT.equalsIgnoreCase(payment.getDateDependency())
                    || SwedbankBaseConstants.PaymentStatus.UNDER_WAY.equalsIgnoreCase(
                            payment.getStatus());
        }
        return false;
    }

    private boolean isTypeSame(String transactionType) {
        return transactionType.equalsIgnoreCase(type);
    }

    @JsonIgnore
    private String getDestinationMessage() {
        if (type == null) {
            return null;
        }

        switch (type.toLowerCase()) {
            case SwedbankBaseConstants.TransactionType.TRANSFER:
                return getRecipientNote();

            case SwedbankBaseConstants.TransactionType.PAYMENT:
                return getPaymentReference();
            default:
                log.warn(UNEXPECTED_TYPE_ERROR, type);
                return null;
        }
    }

    private String getPaymentReference() {
        return Optional.ofNullable(payment)
                .map(PaymentEntity::getReference)
                .map(ReferenceEntity::getValue)
                .orElse(ReturnValue.EMPTY);
    }

    private String getRecipientNote() {
        return Optional.ofNullable(transfer)
                .map(TransferEntity::getNoteToRecipient)
                .orElse(ReturnValue.EMPTY);
    }

    @JsonIgnore
    private RemittanceInformation getRemittanceInformation() {
        if (type == null) {
            return null;
        }
        RemittanceInformation remittanceInformation = new RemittanceInformation();

        switch (type.toLowerCase()) {
            case SwedbankBaseConstants.TransactionType.TRANSFER:
                return createUnstructuredRemittanceInformation(remittanceInformation);

            case SwedbankBaseConstants.TransactionType.PAYMENT:
                return createOcrRemittanceInformation(remittanceInformation);

            default:
                log.warn(UNEXPECTED_TYPE_ERROR, type);
                throw new IllegalStateException("Unexpected transfer type: " + type);
        }
    }

    private RemittanceInformation createUnstructuredRemittanceInformation(
            RemittanceInformation remittanceInformation) {
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        getNoteToRecipient(remittanceInformation);
        return remittanceInformation;
    }

    private RemittanceInformation createOcrRemittanceInformation(
            RemittanceInformation remittanceInformation) {
        remittanceInformation.setType(RemittanceInformationType.OCR);
        getPaymentValue(remittanceInformation);
        return remittanceInformation;
    }

    private void getNoteToRecipient(RemittanceInformation remittanceInformation) {
        remittanceInformation.setValue(getRecipientNote());
    }

    private void getPaymentValue(RemittanceInformation remittanceInformation) {
        remittanceInformation.setValue(getPaymentReference());
    }

    @JsonIgnore
    private String getSourceMessage() {
        if (type == null) {
            return null;
        }
        switch (type.toLowerCase()) {
            case SwedbankBaseConstants.TransactionType.TRANSFER:
                return getTransferAccountName();
            case SwedbankBaseConstants.TransactionType.PAYMENT:
                return getPaymentAccountName();
            default:
                log.warn(UNEXPECTED_TYPE_ERROR, type);
                return null;
        }
    }

    private String getPaymentAccountName() {
        return Optional.ofNullable(payment)
                .map(PaymentEntity::getPayee)
                .map(PayeeEntity::getName)
                .orElse(ReturnValue.EMPTY);
    }

    private String getTransferAccountName() {
        return Optional.ofNullable(noteToSender)
                .filter(note -> !Strings.isNullOrEmpty(note))
                .orElse(
                        Optional.ofNullable(transfer)
                                .map(TransferEntity::getToAccount)
                                .map(ToAccountEntity::getName)
                                .orElse(ReturnValue.EMPTY));
    }

    @JsonIgnore
    private Optional<AccountIdentifier> getDestinationAccount() {
        if (type == null) {
            return Optional.empty();
        }
        switch (type.toLowerCase()) {
            case SwedbankBaseConstants.TransactionType.TRANSFER:
                return getTransferAccountIdentifier();
            case SwedbankBaseConstants.TransactionType.PAYMENT:
                return getPaymentAccountIdentifier();
            default:
                log.warn(UNEXPECTED_TYPE_ERROR, type);
                return Optional.empty();
        }
    }

    private Optional<AccountIdentifier> getPaymentAccountIdentifier() {
        return Optional.ofNullable(payment)
                .map(PaymentEntity::getPayee)
                .map(PayeeEntity::generalGetAccountIdentifier);
    }

    private Optional<AccountIdentifier> getTransferAccountIdentifier() {
        return Optional.ofNullable(transfer)
                .map(TransferEntity::getToAccount)
                .map(ToAccountEntity::getFullyFormattedNumber)
                .map(SwedishIdentifier::new);
    }

    @JsonIgnore
    private TransferType getTransferType() {
        if (type == null) {
            return null;
        }
        switch (type.toLowerCase()) {
            case TransactionType.TRANSFER:
                return TransferType.BANK_TRANSFER;
            case TransactionType.PAYMENT:
                return TransferType.PAYMENT;
            default:
                log.warn(UNEXPECTED_TYPE_ERROR, type);
                return null;
        }
    }

    @JsonIgnore
    private Optional<Transfer> getUpcomingTransfer(AccountIdentifier sourceAccount) {

        Optional<AccountIdentifier> destinationAccount = getDestinationAccount();

        if (isDestinationAccountInvalid(destinationAccount)) {
            return Optional.empty();
        }

        double parsedAmount = StringUtils.parseAmountEU(this.amount);
        if (!Double.isFinite(parsedAmount)) {
            return Optional.empty();
        }
        ExactCurrencyAmount exactCurrencyAmount =
                ExactCurrencyAmount.of(parsedAmount, currencyCode);

        Transfer transfer = new Transfer();
        transfer.setDestination(destinationAccount.get());
        transfer.setSourceMessage(getSourceMessage());
        transfer.setSource(sourceAccount);
        transfer.setDestinationMessage(getDestinationMessage());
        transfer.setRemittanceInformation(getRemittanceInformation());
        transfer.setAmount(exactCurrencyAmount);
        transfer.setDueDate(date);
        transfer.setType(getTransferType());

        return Optional.of(transfer);
    }

    private boolean isDestinationAccountInvalid(Optional<AccountIdentifier> destinationAccount) {
        return !destinationAccount.isPresent()
                || !destinationAccount.get().isValid()
                || Strings.isNullOrEmpty(currencyCode)
                || date == null;
    }
}
