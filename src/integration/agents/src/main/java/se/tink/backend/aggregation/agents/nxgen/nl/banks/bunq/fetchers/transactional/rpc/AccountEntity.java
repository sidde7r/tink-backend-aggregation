package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqPredicates;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.libraries.amount.Amount;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JsonObject
public class AccountEntity {
    @JsonProperty("id")
    private String accountId;
    private String created;
    private String updated;
    private List<AliasEntity> alias;
    private AmountEntity balance;
    private String country;
    private String currency;
    @JsonProperty("daily_limit")
    private AmountEntity dailyLimit;
    @JsonProperty("daily_spent")
    private AmountEntity dailySpent;
    private String description;
    @JsonProperty("public_uuid")
    private String publicUuid;
    private String status;
    @JsonProperty("sub_status")
    private String subStatus;
    private String timeZone;
    @JsonProperty("overdraft_limit")
    private AmountEntity overdraftLimit;

    public String getAccountId() {
        return accountId;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public List<AliasEntity> getAlias() {
        return alias;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public AmountEntity getDailyLimit() {
        return dailyLimit;
    }

    public AmountEntity getDailySpent() {
        return dailySpent;
    }

    public String getDescription() {
        return description;
    }

    public String getPublicUuid() {
        return publicUuid;
    }

    public String getStatus() {
        return status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public AmountEntity getOverdraftLimit() {
        return overdraftLimit;
    }

    public Optional<CheckingAccount> toTinkCheckingAccount() {
        List<AliasEntity> aliasList = Optional.ofNullable(alias).orElse(Collections.emptyList());

        Optional<String> accountIban = aliasList.stream()
                .filter(BunqPredicates.FILTER_IBAN)
                .findFirst()
                .map(AliasEntity::getValue);

        Optional<Amount> balanceAsAmount = Optional.ofNullable(balance)
                .map(AmountEntity::getAsTinkAmount);

        if (!balanceAsAmount.isPresent()) {
            return Optional.empty();
        }

        return accountIban.map(accountNumber -> CheckingAccount.builder(accountNumber, balanceAsAmount.get())
                .setAccountNumber(accountNumber)
                .setName(description)
                .setBankIdentifier(accountId)
                .build());
    }
}
