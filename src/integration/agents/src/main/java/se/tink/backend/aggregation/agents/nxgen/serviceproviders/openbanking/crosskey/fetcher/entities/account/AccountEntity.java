package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountEntity {

    private List<AccountDetailsEntity> account;
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

    public List<AccountDetailsEntity> getAccount() {
        return account;
    }

    public String getAccountId() {
        return accountId;
    }

//    public AccountDetailsEntity resolveUniqueIdentifier() {
//        return getAccountDetails().isPresent() ?
//            getAccountDetails().get() :
//            null;
//    }

    public AccountDetailsEntity resolveAccountDetails() {
        return
            account.stream()
                .filter(AccountDetailsEntity::isIBAN)
                .findFirst()
                .orElseGet(() -> {
                    return account.stream().findFirst().orElse(null);
                });
    }

    public boolean isCheckingAccount() {
        return accountSubType.equalsIgnoreCase(
            AccountSubtypeEntity.CURRENT_ACCOUNT.getKey());
    }

    public boolean isCreditCardAccount() {
        return accountSubType.equalsIgnoreCase(
            AccountSubtypeEntity.CREDIT_CARD.getKey());
    }

    public String getDescription() {
        return description;
    }
}
