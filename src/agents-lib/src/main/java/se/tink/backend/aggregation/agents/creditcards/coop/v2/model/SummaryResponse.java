package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Account;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SummaryResponse {
    @JsonProperty("GetUserSummaryResult")
    private SummaryResultEntity summaryResultEntity;

    public SummaryResultEntity getSummaryResultEntity() {
        return summaryResultEntity;
    }

    public void setSummaryResultEntity(SummaryResultEntity summaryResultEntity) {
        this.summaryResultEntity = summaryResultEntity;
    }

    public List<AccountEntity> getAccounts() {
        if (summaryResultEntity == null) {
            return Collections.emptyList();
        }

        return summaryResultEntity.getAccounts();
    }

    public List<Account> toAccounts() {
        if (summaryResultEntity == null) {
            return Collections.emptyList();
        }

        List<Account> accounts = Lists.newArrayList();
        for (AccountEntity accountEntity : summaryResultEntity.getAccounts()) {
            accounts.add(accountEntity.toAccount());
        }

        return accounts;
    }
}
