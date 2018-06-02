package se.tink.backend.rpc;

import java.util.Date;
import java.util.List;

public class UpdateTransactionRequest {
    private Double amount;
    private String categoryId;
    private Date date;
    private Date originalDate;
    private String description;
    private String notes;
    private List<String> tags;

    public UpdateTransactionRequest() {
    }

    public UpdateTransactionRequest(Double amount, String categoryId, Date date, Date originalDate, String description, String notes) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
        this.originalDate = originalDate;
        this.description = description;
        this.notes = notes;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Date getOriginalDate() {
        return originalDate;
    }
}
