package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenInterestAccountResponse {

    @JsonProperty("currentInterests")
    private List<OpenInterestAccountOption> accountOptions = Lists.newArrayList();

    private String changeDate;

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public List<OpenInterestAccountOption> getAccountOptions() {
        return accountOptions;
    }

    public void setAccountOptions(List<OpenInterestAccountOption> accountOptions) {
        this.accountOptions = accountOptions;
    }
}
