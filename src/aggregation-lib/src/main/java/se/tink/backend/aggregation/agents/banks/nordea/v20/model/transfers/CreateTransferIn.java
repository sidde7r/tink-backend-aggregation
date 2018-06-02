package se.tink.backend.aggregation.agents.banks.nordea.v20.model.transfers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapSerializer;

public class CreateTransferIn
{
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String message;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String toAccountBranchId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String toAccountIdNickname;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String amount;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String recurringFrequency;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String toAccountId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String fromAccountBranchId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String dueDateType;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String fromAccountId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String recurringContinuously;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String dueDate;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String fromAccountProductTypeExtension;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String recurringNumberOfPayments;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String currency;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDueDateType() {
        return dueDateType;
    }

    public void setDueDateType(String dueDateType) {
        this.dueDateType = dueDateType;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRecurringNumberOfPayments() {
        return recurringNumberOfPayments;
    }

    public void setRecurringNumberOfPayments(String recurringNumberOfPayments) {
        this.recurringNumberOfPayments = recurringNumberOfPayments;
    }

    public String getFromAccountProductTypeExtension() {
        return fromAccountProductTypeExtension;
    }

    public void setFromAccountProductTypeExtension(String fromAccountProductTypeExtension) {
        this.fromAccountProductTypeExtension = fromAccountProductTypeExtension;
    }

    public String getRecurringContinuously() {
        return recurringContinuously;
    }

    public void setRecurringContinuously(String recurringContinuously) {
        this.recurringContinuously = recurringContinuously;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getFromAccountBranchId() {
        return fromAccountBranchId;
    }

    public void setFromAccountBranchId(String fromAccountBranchId) {
        this.fromAccountBranchId = fromAccountBranchId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getRecurringFrequency() {
        return recurringFrequency;
    }

    public void setRecurringFrequency(String recurringFrequency) {
        this.recurringFrequency = recurringFrequency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToAccountBranchId() {
        return toAccountBranchId;
    }

    public void setToAccountBranchId(String toAccountBranchId) {
        this.toAccountBranchId = toAccountBranchId;
    }

    public String getToAccountIdNickname() {
        return toAccountIdNickname;
    }

    public void setToAccountIdNickname(String toAccountIdNickname) {
        this.toAccountIdNickname = toAccountIdNickname;
    }
}