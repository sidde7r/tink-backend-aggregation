package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionItem {

    @JsonProperty("date")
    private Date date;

    @JsonProperty("counterparty_name")
    private String counterpartyName;

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("is_internal")
    private boolean isInternal;

    @JsonProperty("counterparty_account_no")
    private String counterpartyAccountNo;

    @JsonProperty("description")
    private String description;

    @JsonProperty("confirmation")
    private Confirmation confirmation;

    @JsonProperty("type")
    private Type type;

    @JsonProperty("is_completed")
    private boolean completed;

    @JsonProperty("payment_number")
    private Object paymentNumber;

    @JsonProperty("can_use_as_template")
    private boolean canUseAsTemplate;

    @JsonProperty("transaction_reference")
    private String transactionReference;

    @JsonProperty("reference_no")
    private Object referenceNo;

    @JsonProperty("currency_id")
    private int currencyId;

    public Date getDate() {
        return date;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isIsInternal() {
        return isInternal;
    }

    public String getCounterpartyAccountNo() {
        return counterpartyAccountNo;
    }

    public String getDescription() {
        return description;
    }

    public Confirmation getConfirmation() {
        return confirmation;
    }

    public Type getType() {
        return type;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Object getPaymentNumber() {
        return paymentNumber;
    }

    public boolean isCanUseAsTemplate() {
        return canUseAsTemplate;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Object getReferenceNo() {
        return referenceNo;
    }

    public int getCurrencyId() {
        return currencyId;
    }
}
