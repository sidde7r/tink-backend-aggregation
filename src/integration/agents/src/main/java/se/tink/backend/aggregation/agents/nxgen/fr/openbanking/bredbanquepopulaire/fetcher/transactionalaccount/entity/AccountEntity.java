package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@Slf4j
public class AccountEntity {
    private String resourceId;
    private String bicFi;
    private AccountId accountId;
    private String name;
    private AccountUsage usage;
    private CashAccountType cashAccountType;
    private String currency;
    private String product;
    private String details;
    private String linkedAccount;

    @JsonProperty("_links")
    private AccountsLinksEntity links;

    private List<BalanceEntity> balances;

    @JsonIgnore
    public boolean isTransactionalAccount() {
        final boolean isTransactional = CashAccountType.CACC == cashAccountType;
        if (!isTransactional) {
            log.info("CashAccountType other than CACC. Current value: {}", cashAccountType);
        }
        return isTransactional;
    }

    @JsonIgnore
    public boolean isCreditCardAccount() {
        return CashAccountType.CARD == cashAccountType;
    }

    @JsonIgnore
    public boolean containsBalances() {
        return balances != null;
    }
}
