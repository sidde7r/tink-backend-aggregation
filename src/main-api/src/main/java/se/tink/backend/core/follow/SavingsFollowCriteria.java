package se.tink.backend.core.follow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsFollowCriteria extends FollowCriteria {
    private List<String> accountIds;
    private String targetPeriod;

    public List<String> getAccountIds() {
        return accountIds;
    }

    public String getTargetPeriod() {
        return targetPeriod;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds;
    }

    public void setTargetPeriod(String targetPeriod) {
        this.targetPeriod = targetPeriod;
    }
}
