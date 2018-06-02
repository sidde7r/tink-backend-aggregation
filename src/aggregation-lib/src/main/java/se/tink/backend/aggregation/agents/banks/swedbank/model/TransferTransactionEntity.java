package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferTransactionEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AggregationLogger log = new AggregationLogger(TransferTransactionEntity.class);

    private TransactionAccountEntity fromAccount;
    private LinksEntity links;
    private String id;
    private String type;
    private String date;
    private String amount;
    private String bookedDate;
    private TransferTransactionDetails transfer;
    private TransferTransactionDetails payment;
    private String noteToSender;
    private List<ErrorMessage> rejectionCauses;

    public TransactionAccountEntity getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(TransactionAccountEntity fromAccount) {
        this.fromAccount = fromAccount;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return Strings.isNullOrEmpty(type) ? null : type.toUpperCase();
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBookedDate() {
        return bookedDate;
    }

    public void setBookedDate(String bookedDate) {
        this.bookedDate = bookedDate;
    }

    public TransferTransactionDetails getTransfer() {
        return transfer;
    }

    public void setTransfer(TransferTransactionDetails transfer) {
        this.transfer = transfer;
    }

    public TransferTransactionDetails getPayment() {
        return payment;
    }

    public void setPayment(TransferTransactionDetails payment) {
        this.payment = payment;
    }

    public String getNoteToSender() {
        return noteToSender;
    }

    public void setNoteToSender(String noteToSender) {
        this.noteToSender = noteToSender;
    }

    public List<ErrorMessage> getRejectionCauses() {
        return rejectionCauses;
    }

    public void setRejectionCauses(List<ErrorMessage> rejectionCauses) {
        this.rejectionCauses = rejectionCauses;
    }

    @JsonIgnore
    public TransferTransactionDetails getTransactionDetails() {
        if (Objects.equal(getType(), "TRANSFER")) {
            return transfer;
        } else if (Objects.equal(getType(), "PAYMENT")) {
            return payment;
        }
        return null;
    }

    public Transaction toTransaction(Transfer editableTransfer) throws Exception {
        Transaction transaction = toTransaction();

        editableTransfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));

        transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                MAPPER.writeValueAsString(editableTransfer));

        transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                UUIDUtils.toTinkUUID(editableTransfer.getId()));

        return transaction;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();

        try {
            transaction.setAmount(-1 * getTinkAmount());

            if (Objects.equal(getType(), "PAYMENT")) {
                transaction.setDescription(getTinkPayeeName());
            } else if (Objects.equal(getType(), "TRANSFER")) {
                transaction.setDescription(getTinkToAccountName());
            }
            transaction.setDate(getTinkDate());
            transaction.setPending(true);
        } catch (IllegalStateException e) {
            log.error("Couldn't parse transaction", e);
        }

        return transaction;
    }

    private double getTinkAmount() {
        Preconditions.checkState(!Strings.isNullOrEmpty(amount));
        return StringUtils.parseAmount(amount);
    }

    private String getTinkPayeeName() {
        PaymentAccountEntity payee = getPayment().getPayee();
        Preconditions.checkState(!(payee == null || Strings.isNullOrEmpty(payee.getName())));
        return payee.getName();
    }

    private String getTinkToAccountName() {
        TransactionAccountEntity toAccount = getTransfer().getToAccount();
        Preconditions.checkState(!(toAccount == null || Strings.isNullOrEmpty(toAccount.getName())));
        return toAccount.getName();
    }

    private Date getTinkDate() {
        Preconditions.checkState(!Strings.isNullOrEmpty(getDate()));
        return DateUtils.flattenTime(DateUtils.parseDate(date));
    }
}
