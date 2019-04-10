package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {
    @JsonProperty("OwnAccounts")
    private List<AccountEntity> ownAccounts;

    @JsonProperty("JointAccounts")
    private List<AccountEntity> jointAccounts;

    @JsonProperty("MinorsAccounts")
    private List<AccountEntity> minorsAccounts;

    public List<AccountEntity> getOwnAccounts() {
        return ownAccounts;
    }

    public List<AccountEntity> getJointAccounts() {
        return jointAccounts;
    }

    public List<AccountEntity> getMinorsAccounts() {
        return minorsAccounts;
    }

    public List<AccountEntity> getAllAccounts() {
        List<AccountEntity> accounts = Lists.newArrayList();

        accounts.addAll(Optional.ofNullable(ownAccounts).orElse(Collections.emptyList()));
        accounts.addAll(Optional.ofNullable(jointAccounts).orElse(Collections.emptyList()));
        accounts.addAll(Optional.ofNullable(minorsAccounts).orElse(Collections.emptyList()));

        return accounts;
    }
}
