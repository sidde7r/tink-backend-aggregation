package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoansResponse {
    private List<LoanEntity> loans;
    private List<LinkEntity> links;

    public List<LoanEntity> getLoans() {
        return loans;
    }

    public void setLoans(List<LoanEntity> loans) {
        this.loans = loans;
    }

    public List<LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(List<LinkEntity> links) {
        this.links = links;
    }
}
