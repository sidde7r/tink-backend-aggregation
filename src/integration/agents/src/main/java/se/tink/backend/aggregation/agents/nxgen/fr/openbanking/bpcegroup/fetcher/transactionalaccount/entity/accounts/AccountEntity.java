package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@Slf4j
@Builder
public class AccountEntity {

    private String cashAccountType;
    private AccountId accountId;
    private String resourceId;
    private String product;

    @JsonProperty("_links")
    private NavigationLinksEntity links;

    private String usage;
    private String psuStatus;
    private String name;
    private String bicFi;
    private String currency;
    private String details;
    private String linkedAccount;
    private List<BalanceEntity> balances;

    private String holderName;

    @JsonIgnore
    public boolean isTransactionalAccount() {
        logAccountTypeIfUnknown();
        return AccountType.TRANSACTIONAL.getType().equalsIgnoreCase(cashAccountType);
    }

    private void logAccountTypeIfUnknown() {
        if (!AccountType.TRANSACTIONAL.getType().equalsIgnoreCase(cashAccountType)
                && !AccountType.CARD.getType().equalsIgnoreCase(cashAccountType)) {
            log.debug("Unknown account type {}", cashAccountType);
        }
    }

    @JsonIgnore
    public boolean isCard() {
        return AccountType.CARD.getType().equalsIgnoreCase(cashAccountType);
    }

    @JsonIgnore
    public boolean containsBalances() {
        return balances != null;
    }

    @JsonIgnore
    public String getIban() {
        return getAccountId().getIban();
    }
}
