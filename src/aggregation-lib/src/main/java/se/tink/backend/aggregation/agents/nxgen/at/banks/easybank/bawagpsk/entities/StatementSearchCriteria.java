package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlElement;

public class StatementSearchCriteria {
    LocalDateTime minDatePosted;
    LocalDateTime maxDatePosted;
    String sortingColumn;
    String transactionType;

    @XmlElement(name = "MinDatePosted")
    public void setMinDatePosted(LocalDateTime minDatePosted) {
        this.minDatePosted = minDatePosted;
    }

    public LocalDateTime getMinDatePosted() {
        return minDatePosted;
    }

    @XmlElement(name = "MaxDatePosted")
    public void setMaxDatePosted(LocalDateTime maxDatePosted) {
        this.maxDatePosted = maxDatePosted;
    }

    public LocalDateTime getMaxDatePosted() {
        return maxDatePosted;
    }

    @XmlElement(name = "SortingColumn")
    public void setSortingColumn(String sortingColumn) {
        this.sortingColumn = sortingColumn;
    }

    public String getSortingColumn() {
        return sortingColumn;
    }

    @XmlElement(name = "TransactionType")
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
