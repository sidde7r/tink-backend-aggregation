package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.banks.danskebank.DanskeUtils;
import se.tink.backend.aggregation.agents.models.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    public static final Predicate<TransactionEntity> IS_DELETED_OR_REJECTED =
            new Predicate<TransactionEntity>() {
                @Override
                public boolean apply(@Nullable TransactionEntity transactionEntity) {
                    if (transactionEntity != null) {
                        String stateCode = transactionEntity.getStateCode();

                        return Objects.equal(stateCode, STATE_DELETED) || Objects.equal(stateCode, STATE_REJECTED);
                    }

                    return true;
                }
            };
    private static final String STATE_REJECTED = "A";
    private static final String STATE_DELETED = "D";
    private static final String STATE_WAITING = "W";

    @JsonProperty("Account")
    private String account;
    @JsonProperty("Amount")
    private double amount;
    @JsonProperty("AttachmentType")
    private String attachmentType;
    @JsonProperty("Balance")
    private double balance;
    @JsonProperty("Category")
    private String category;
    @JsonProperty("PaymentKey")
    private String paymentKey;
    @JsonProperty("PaymentType")
    private String paymentType;
    @JsonProperty("Reconciled")
    private boolean reconciled;
    @JsonProperty("ShowBalance")
    private boolean showBalance;
    @JsonProperty("ShowReconciliation")
    private boolean showReconciliation;
    @JsonProperty("StateCode")
    private String stateCode;
    @JsonProperty("StateText")
    private String stateText;
    @JsonProperty("Text")
    private String text;
    @JsonProperty("Time")
    private String time;
    @JsonProperty("TransactionId")
    private String transactionId;

    public String getAccount() {
        return account;
    }

    public double getAmount() {
        return amount;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public double getBalance() {
        return balance;
    }

    public String getCategory() {
        return category;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getStateText() {
        return stateText;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public boolean isShowBalance() {
        return showBalance;
    }

    public boolean isShowReconciliation() {
        return showReconciliation;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }

    public void setShowBalance(boolean showBalance) {
        this.showBalance = showBalance;
    }

    public void setShowReconciliation(boolean showReconciliation) {
        this.showReconciliation = showReconciliation;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public void setStateText(String stateText) {
        this.stateText = stateText;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setDescription(text);
        transaction.setDate(DanskeUtils.parseDanskeDate(time));
        transaction.setType(DanskeUtils.getTinkTransactionType(category));
        
        if (Objects.equal(stateCode, STATE_WAITING)) {
            transaction.setPending(true);
        }

        return transaction;
    }
}
