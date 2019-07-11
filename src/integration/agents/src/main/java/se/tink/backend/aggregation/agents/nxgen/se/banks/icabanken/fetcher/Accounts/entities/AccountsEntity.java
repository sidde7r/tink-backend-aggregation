package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {
    private static final Logger log = LoggerFactory.getLogger(AccountsEntity.class);

    @JsonProperty("OwnAccounts")
    private List<AccountEntity> ownAccounts;

    @JsonProperty("JointAccounts")
    private List<AccountEntity> jointAccounts;

    @JsonProperty("MinorsAccounts")
    private List<AccountEntity> minorsAccounts;

    public List<AccountEntity> getOwnAccounts() {
        if (ownAccounts == null) {
            log.warn("Expected ownAccounts to be an empty list, was null.");
            return Collections.emptyList();
        }

        return ownAccounts;
    }

    public List<AccountEntity> getJointAccounts() {
        if (jointAccounts == null) {
            log.warn("Expected jointAccounts to be an empty list, was null.");
            return Collections.emptyList();
        }

        return jointAccounts;
    }

    public List<AccountEntity> getMinorsAccounts() {
        if (minorsAccounts == null) {
            log.warn("Expected minorsAccounts to be an empty list, was null.");
            return Collections.emptyList();
        }

        return minorsAccounts;
    }

    @JsonIgnore
    public List<AccountEntity> getAllAccounts() {
        List<AccountEntity> accounts = Lists.newArrayList();

        accounts.addAll(getOwnAccounts());
        accounts.addAll(getJointAccounts());
        accounts.addAll(getMinorsAccounts());

        return accounts;
    }

        return accounts;
    }
}
