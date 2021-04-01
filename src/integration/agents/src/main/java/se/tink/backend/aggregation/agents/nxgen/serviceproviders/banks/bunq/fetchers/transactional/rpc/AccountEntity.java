package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities.AliasEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public Optional<TransactionalAccount> toTinkAccount(TransactionalAccountType accountType) {
        List<AliasEntity> aliasList = Optional.ofNullable(alias).orElseGet(Collections::emptyList);

        Optional<AliasEntity> accountIban =
                aliasList.stream().filter(BunqPredicates.FILTER_IBAN).findFirst();

        Optional<ExactCurrencyAmount> balanceAsAmount =
                Optional.ofNullable(balance).map(AmountEntity::getAsTinkAmount);

        return balanceAsAmount.flatMap(
                balance ->
                        accountIban.flatMap(
                                account ->
                                        TransactionalAccount.nxBuilder()
                                                .withType(accountType)
                                                .withPaymentAccountFlag()
                                                .withBalance(getBalanceModule())
                                                .withId(getIdModule(account.getValue()))
                                                .addHolderName(account.getName())
                                                .setApiIdentifier(accountId)
                                                .setBankIdentifier(account.getValue())
                                                .build()));
    }

    private IdModule getIdModule(String accountNumber) {
        return IdModule.builder()
                .withUniqueIdentifier(accountNumber)
                .withAccountNumber(accountNumber)
                .withAccountName(description)
                .addIdentifier(new IbanIdentifier(accountNumber))
                .build();
    }

    // Use default balance from the bank
    private BalanceModule getBalanceModule() {
        return BalanceModule.builder().withBalance(balance.getAsTinkAmount()).build();
    }
}
