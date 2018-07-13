package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Invoice {

    private String id;
    private LinksEntity links;
    private String amount;
    private Date dueDate;
    private String payeeName;
    private boolean dueDatePassed;
    private DetailDocument detailDocument;

    public String getAmount() {
        return amount;
    }

    public DetailDocument getDetailDocument() {
        return detailDocument;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getId() {
        return id;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public boolean isDueDatePassed() {
        return dueDatePassed;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDetailDocument(DetailDocument detailDocument) {
        this.detailDocument = detailDocument;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setDueDatePassed(boolean dueDatePassed) {
        this.dueDatePassed = dueDatePassed;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }
}
