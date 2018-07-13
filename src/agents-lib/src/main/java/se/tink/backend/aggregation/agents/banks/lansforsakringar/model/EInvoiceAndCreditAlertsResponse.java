package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceAndCreditAlertsResponse {
    private int numberOfNewInvoices;
    private int numberOfNewCredits;
    private int totalNumberOfAlerts;

    public int getNumberOfNewInvoices() {
        return numberOfNewInvoices;
    }

    public void setNumberOfNewInvoices(int numberOfNewInvoices) {
        this.numberOfNewInvoices = numberOfNewInvoices;
    }

    public int getNumberOfNewCredits() {
        return numberOfNewCredits;
    }

    public void setNumberOfNewCredits(int numberOfNewCredits) {
        this.numberOfNewCredits = numberOfNewCredits;
    }

    public int getTotalNumberOfAlerts() {
        return totalNumberOfAlerts;
    }

    public void setTotalNumberOfAlerts(int totalNumberOfAlerts) {
        this.totalNumberOfAlerts = totalNumberOfAlerts;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("numberOfNewInvoices", numberOfNewInvoices)
                .add("numberOfNewCredits", numberOfNewCredits)
                .add("totalNumberOfAlerts", totalNumberOfAlerts)
                .toString();
    }
}
