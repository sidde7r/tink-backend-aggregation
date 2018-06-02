package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanListResponse {
	private String totalRemainingDebt;
	private List<LoanEntity> loans;

	public List<LoanEntity> getLoans() {
		return loans;
	}

	public void setLoans(List<LoanEntity> loans) {
		this.loans = loans;
	}

    public String getTotalRemainingDebt() {
        return totalRemainingDebt;
    }

    public void setTotalRemainingDebt(String totalRemainingDebt) {
        this.totalRemainingDebt = totalRemainingDebt;
    }
}
