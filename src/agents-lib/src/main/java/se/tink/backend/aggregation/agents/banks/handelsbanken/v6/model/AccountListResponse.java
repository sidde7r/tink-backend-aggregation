package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.google.common.collect.Lists;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse extends AbstractResponse{
    private List<AccountGroupEntity> accountGroups;

    public List<AccountGroupEntity> getAccountGroups() {
        return accountGroups;
    }

    public void setAccountGroups(List<AccountGroupEntity> accountGroups) {
        this.accountGroups = accountGroups;
    }

    public List<AccountEntity> toAccountEntityList() {
        List<AccountEntity> entities = Lists.newArrayList();
        if (getAccountGroups() != null) {
            for (AccountGroupEntity accountGroupEntity : getAccountGroups()) {
                if (accountGroupEntity.getAccounts() == null) {
                    continue;
                }

                entities.addAll(accountGroupEntity.getAccounts());
            }
        }
        return entities;
    }
}
