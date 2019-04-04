package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountEntity {
    private List<AccountDetailsEntity> accounts;
    private String accountId;
    private String accountSubType;
    private String accountType;
    private String currency;
    private String description;
    private String nickname;
    private ServicerEntity servicer;

    public String getCurrency() {
        return currency;
    }

    public List<AccountDetailsEntity> getAccounts() {
        return accounts;
    }

    public String getAccountId() {
        return accountId;
    }

    public Optional<AccountDetailsEntity> resolveAccountDetails() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(AccountDetailsEntity::isIBAN)
                .findFirst();
    }

    public boolean isCheckingAccount() {
        return accountSubType.equalsIgnoreCase(AccountSubtypeEntity.CURRENT_ACCOUNT.getKey());
    }

    public boolean isCreditCardAccount() {
        return accountSubType.equalsIgnoreCase(AccountSubtypeEntity.CREDIT_CARD.getKey());
    }

    public String getDescription() {
        return description;
    }
}
