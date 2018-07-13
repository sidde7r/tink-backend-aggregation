package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.LansforsakringarAgent.SavingsAccountTypes;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenInterestAccountOption {

    private Long changeDate;
    private String description;
    private Double rate;

    public Long getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Long changeDate) {
        this.changeDate = changeDate;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
    
    public static SavingsAccountTypes getAccountType(String description) {
        if (description.toLowerCase().contains("fastränte")) {
            return SavingsAccountTypes.fixedrate;
        }
        if (description.toLowerCase().contains("länskonto")) {
            return SavingsAccountTypes.county;
        }
        if (description.toLowerCase().contains("sparkonto")) {
            return SavingsAccountTypes.saving;
        }
        return null;
    }
}
