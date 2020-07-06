package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("availableBalance")
    private BigDecimal availableBalance;

    @JsonProperty("bookedBalance")
    private BigDecimal bookedBalance;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("description")
    private String description;

    @JsonProperty("id")
    private String id;

    @JsonProperty("accountType")
    private String accountType;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(List<AccountHolder> accountHolders) {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(getBalance())
                                .setAvailableBalance(getAvailableBalance())
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(description)
                                .addIdentifier(new IbanIdentifier(accountNumber))
                                .build())
                .addHolders(
                        accountHolders.stream()
                                .map(AccountHolder::getName)
                                .map(Holder::of)
                                .collect(Collectors.toList()))
                .setApiIdentifier(getId())
                .build()
                .get();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(availableBalance, currency);
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(bookedBalance, currency);
    }

    @JsonIgnore
    private TransactionalAccountType getAccountType() {
        if (AccountTypes.CHECKING.equalsIgnoreCase(accountType)) {
            return TransactionalAccountType.CHECKING;
        } else return TransactionalAccountType.SAVINGS;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }
}
