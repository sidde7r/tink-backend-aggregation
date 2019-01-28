package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto;

import com.google.common.base.Strings;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.MessageType;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.date.CountryDateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransferRequest {
    private static final ThreadSafeDateFormat FORMATTER_DUE_DATE = new ThreadSafeDateFormat("ddMMyyyy");

    private TypeEncValueTuple principalAccountNo;
    private TypeEncValueTuple principalName;
    private TypeValuePair amount;
    private TypeValuePair executionDate;
    private TypeValuePair referenceStructured;
    private TypeValuePair beneficiaryAccountNo;
    private TypeEncValueTuple currency;
    private TypeEncValueTuple principalCurrencyCode;
    private TypeValuePair transferTypeCode;
    private TypeValuePair scashVersionNumber;
    private TypeValuePair functionName;
    private TypeValuePair beneficiaryName;
    private TypeValuePair referenceFreeText;

    public TransferRequest(
            TypeEncValueTuple principalAccountNo,
            TypeEncValueTuple principalName,
            TypeValuePair amount,
            TypeValuePair executionDate,
            TypeValuePair referenceStructured,
            TypeValuePair beneficiaryAccountNo,
            TypeEncValueTuple currency,
            TypeEncValueTuple principalCurrencyCode,
            TypeValuePair transferTypeCode,
            TypeValuePair scashVersionNumber,
            TypeValuePair functionName,
            TypeValuePair beneficiaryName,
            TypeValuePair referenceFreeText) {
        this.principalAccountNo = principalAccountNo;
        this.principalName = principalName;
        this.amount = amount;
        this.executionDate = executionDate;
        this.referenceStructured = referenceStructured;
        this.beneficiaryAccountNo = beneficiaryAccountNo;
        this.currency = currency;
        this.principalCurrencyCode = principalCurrencyCode;
        this.transferTypeCode = transferTypeCode;
        this.scashVersionNumber = scashVersionNumber;
        this.functionName = functionName;
        this.beneficiaryName = beneficiaryName;
        this.referenceFreeText = referenceFreeText;
    }

    private static String formatDueDate(Date date) {
        return FORMATTER_DUE_DATE.format(date);
    }

    private static String formatAmount(Amount amount) {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');
        return new DecimalFormat("0.00", decimalSymbols).format(amount.getValue());
    }

    public static String formatStructuredReference(String reference) {
        return reference.replaceAll("[+/ ]", "");
    }

    private static void validateAmount(Amount amount) {
        if (amount.isLessThan(KbcConstants.Transfers.MIN_AMOUNT)) {
            cancelTransfer(KbcConstants.TransferMessageParametrized.AMOUNT_LESS_THAN_MIN.cloneWith(KbcConstants.Transfers.MIN_AMOUNT).get());
        }
    }

    private static Date getValidatedDueDate(Transfer transfer) {
        Date dueDate = transfer.getDueDate() != null ? transfer.getDueDate() : new Date();
        validateDueDate(dueDate);

        return dueDate;
    }

    private static void validateDueDate(Date date) {
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        CountryDateUtils belgianDateUtils = CountryDateUtils.getBelgianDateUtils();

        if (date.after(nextYear.getTime())) {
            cancelTransfer(KbcConstants.TransferMessage.DUE_DATE_TOO_FAR_IN_FUTURE.getKey().get());
        }

        if (!belgianDateUtils.isBusinessDay(date)) {
            cancelTransfer(TransferExecutionException.EndUserMessage.INVALID_DUEDATE_NOT_BUSINESSDAY.getKey().get());
        }
    }

    private static boolean isValidReferenceFreeText(String destinationMessage) {
        if (destinationMessage.length() > KbcConstants.Transfers.MAX_MSG_LENGTH) {
            cancelTransfer(KbcConstants.TransferMessageParametrized.MSG_LENGTH_EXCEEDS_MAX.cloneWith(KbcConstants.Transfers.MAX_MSG_LENGTH).get());
        }

        return true;
    }

    private static String getValidatedPrincipalName(Transfer transfer) {
        Optional<String> principalName = transfer.getSource().getName();

        if (!principalName.isPresent() || Strings.isNullOrEmpty(principalName.get())) {
            cancelTransfer(KbcConstants.TransferMessage.MISSING_SOURCE_NAME.getKey().get());
        }

        return principalName.get();
    }

    private static String getValidatedBeneficiaryName(Transfer transfer) {
        Optional<String> beneficiaryName = transfer.getDestination().getName();

        if (!beneficiaryName.isPresent() || Strings.isNullOrEmpty(beneficiaryName.get())) {
            cancelTransfer(KbcConstants.TransferMessage.MISSING_DESTINATION_NAME.getKey().get());
        }

        return beneficiaryName.get();
    }

    public static TransferRequest create(Transfer transfer, AgreementDto sourceAccount, boolean isTransferToOwnAccount) {
        String beneficiaryAccountNo = ((SepaEurIdentifier)transfer.getDestination()).getIban();

        MessageType messageType = transfer.getMessageType();
        String destinationMessage = transfer.getDestinationMessage();
        String referenceStructured = messageType == MessageType.STRUCTURED ?
                formatStructuredReference(destinationMessage) : "";
        String referenceFreeText = (messageType == MessageType.FREE_TEXT
                && isValidReferenceFreeText(destinationMessage)) ? destinationMessage : "";

        validateAmount(transfer.getAmount());
        String beneficiaryName = getValidatedBeneficiaryName(transfer);
        Date dueDate = getValidatedDueDate(transfer);
        return TransferRequest.builder()
                .setPrincipalAccountNo(sourceAccount.getAgreementNo())
                .setPrincipalName(sourceAccount.getAgreementName())
                .setAmount(formatAmount(transfer.getAmount()))
                .setExecutionDate(formatDueDate(dueDate))
                .setReferenceStructured(referenceStructured)
                .setBeneficiaryAccountNo(beneficiaryAccountNo)
                .setCurrency(sourceAccount.getCurrency())
                .setPrincipalCurrencyCode(sourceAccount.getCurrency())
                .setTransferTypeCode(KbcConstants.Transfers.TRANSFER_TYPE_CODE)
                .setScashVersionNumber(KbcConstants.Transfers.SCASH_VERSION_NUMBER)
                .setFunctionName(isTransferToOwnAccount ? KbcConstants.Transfers.TRANSFER_TO_OWN_ACCOUNT :
                        KbcConstants.Transfers.TRANSFER_TO_OTHER_ACCOUNT)
                .setBeneficiaryName(beneficiaryName)
                .setReferenceFreeText(referenceFreeText)
                .build();
    }

     private static void cancelTransfer(String message) throws TransferExecutionException {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(message)
                .setMessage(String.format("Error when executing transfer: %s", message))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TypeEncValueTuple principalAccountNo;
        private TypeEncValueTuple principalName;
        private TypeValuePair amount;
        private TypeValuePair executionDate;
        private TypeValuePair referenceStructured;
        private TypeValuePair beneficiaryAccountNo;
        private TypeEncValueTuple currency;
        private TypeEncValueTuple principalCurrencyCode;
        private TypeValuePair transferTypeCode;
        private TypeValuePair scashVersionNumber;
        private TypeValuePair functionName;
        private TypeValuePair beneficiaryName;
        private TypeValuePair referenceFreeText;

        public Builder setPrincipalAccountNo(TypeEncValueTuple principalAccountNo) {
            this.principalAccountNo = principalAccountNo;
            return this;
        }

        public Builder setPrincipalName(TypeEncValueTuple principalName) {
            this.principalName = principalName;
            return this;
        }

        public Builder setAmount(String amount) {
            this.amount = TypeValuePair.create(KbcConstants.PairTypeTypes.DECIMAL, amount);
            return this;
        }

        public Builder setExecutionDate(String executionDate) {
            this.executionDate = TypeValuePair.create(KbcConstants.PairTypeTypes.DATE, executionDate);
            return this;
        }

        public Builder setReferenceStructured(String referenceStructured) {
            this.referenceStructured =
                    TypeValuePair.create(KbcConstants.PairTypeTypes.REFERENCE_STRUCTURED, referenceStructured);
            return this;
        }

        public Builder setBeneficiaryAccountNo(String beneficiaryAccountNo) {
            this.beneficiaryAccountNo = TypeValuePair.create(KbcConstants.PairTypeTypes.IBAN, beneficiaryAccountNo);
            return this;
        }

        public Builder setCurrency(TypeEncValueTuple currency) {
            this.currency = currency;
            return this;
        }

        public Builder setPrincipalCurrencyCode(TypeEncValueTuple principalCurrencyCode) {
            this.principalCurrencyCode = principalCurrencyCode;
            return this;
        }

        public Builder setTransferTypeCode(String transferTypeCode) {
            this.transferTypeCode = TypeValuePair.createText(transferTypeCode);
            return this;
        }

        public Builder setScashVersionNumber(String scashVersionNumber) {
            this.scashVersionNumber = TypeValuePair.createText(scashVersionNumber);
            return this;
        }

        public Builder setFunctionName(String functionName) {
            this.functionName = TypeValuePair.createText(functionName);
            return this;
        }

        public Builder setBeneficiaryName(String beneficiaryName) {
            this.beneficiaryName = TypeValuePair.createText(beneficiaryName);
            return this;
        }

        public Builder setReferenceFreeText(String referenceFreeText) {
            this.referenceFreeText = TypeValuePair.createText(referenceFreeText);
            return this;
        }

        public TransferRequest build() {
            return new TransferRequest(principalAccountNo, principalName, amount, executionDate, referenceStructured,
                    beneficiaryAccountNo, currency, principalCurrencyCode, transferTypeCode, scashVersionNumber,
                    functionName, beneficiaryName, referenceFreeText);
        }
    }
}
