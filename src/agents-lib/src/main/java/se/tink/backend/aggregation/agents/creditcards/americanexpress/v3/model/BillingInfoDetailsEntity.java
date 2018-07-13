package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class BillingInfoDetailsEntity {
    private String billingIndex;
    private String title;
    private String label;
    private String startDate;
    private String endDate;

    public String getBillingIndex() {
        return billingIndex;
    }

    public void setBillingIndex(String billingIndex) {
        this.billingIndex = billingIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
