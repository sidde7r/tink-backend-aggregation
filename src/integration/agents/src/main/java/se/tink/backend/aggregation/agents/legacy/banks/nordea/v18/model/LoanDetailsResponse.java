package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement()
public class LoanDetailsResponse {

    @JsonProperty("getLoanDetailsOut")
    private LoanDetailsEntity loanDetails;

    public LoanDetailsEntity getLoanDetails() {
        return loanDetails;
    }

    public void setLoanDetails(LoanDetailsEntity loanDetails) {
        this.loanDetails = loanDetails;
    }
}
