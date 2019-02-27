package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class OpBankTransactionEntity {
    private String encryptedTransactionId;
    private String archiveId;
    private String encryptedArchiveId;
    private String timeStamp;
    private String recipientName;
    private String amount;
    private String transactionState;
    private String paymentType;
    private int transactionTypeCode;
    private String transactionTypeDescription;
    private String transactionMessage;
    private String total;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfEntry;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfValue;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfPayment;
    private boolean deposit;
    private boolean ownTransfer;
    private boolean withdrawal;
    private String bicRecipient;
    private String ibanRecipient;
    private String payerName;
    private String nameSourceCode;
    private String recipientAddress;
    private String payerAddress;

    @JsonIgnore
    public Transaction toTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(AgentParsingUtils.parseAmount(amount)))
                .setDescription(getDescription())
                .setDate(getDate())
                .setPending(dateOfValue == null)
                .build();
    }

    @JsonIgnore
    private String getDescription() {
        if (AgentParsingUtils.parseAmount(amount) > 0 && !Strings.isNullOrEmpty(payerName)) {
            return payerName;
        }

        if (!Strings.isNullOrEmpty(recipientName)) {
            return recipientName;
        }

        if (!Strings.isNullOrEmpty(payerName)) {
            return payerName;
        }

        return transactionTypeDescription;
    }

    // This logic is a bit arbitrary, of what we've seen these contain the same dates. Using entry date as fallback
    // in case the others are null.
    @JsonIgnore
    private Date getDate() {
        if (dateOfValue != null) {
            return dateOfValue;
        }

        if (dateOfPayment != null) {
            return dateOfPayment;
        }

        return dateOfEntry;
    }

    public String getEncryptedTransactionId() {
        return encryptedTransactionId;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public String getEncryptedArchiveId() {
        return encryptedArchiveId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getAmount() {
        return amount;
    }

    public String getTransactionState() {
        return transactionState;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public int getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public String getTransactionTypeDescription() {
        return transactionTypeDescription;
    }

    public String getTransactionMessage() {
        return transactionMessage;
    }

    public String getTotal() {
        return total;
    }

    public Date getDateOfEntry() {
        return dateOfEntry;
    }

    public Date getDateOfValue() {
        return dateOfValue;
    }

    public Date getDateOfPayment() {
        return dateOfPayment;
    }

    public boolean isDeposit() {
        return deposit;
    }

    public boolean isOwnTransfer() {
        return ownTransfer;
    }

    public boolean isWithdrawal() {
        return withdrawal;
    }

    public String getBicRecipient() {
        return bicRecipient;
    }

    public String getIbanRecipient() {
        return ibanRecipient;
    }

    public String getPayerName() {
        return payerName;
    }

    public String getNameSourceCode() {
        return nameSourceCode;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public String getPayerAddress() {
        return payerAddress;
    }
}
