package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("availableBalanceCents")
    private Integer availableBalanceCents;

    @JsonProperty("bookedBalanceCents")
    private Integer bookedBalanceCents;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("description")
    private String description;

    @JsonProperty("id")
    private String id;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(description)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SE, accountNumber))
                                .build())
                .setApiIdentifier(accountNumber)
                .setBankIdentifier(accountNumber)
                .build()
                .get();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(BigDecimal.valueOf(bookedBalanceCents), currency);
    }

    public String getId() {
        return id;
    }
}
