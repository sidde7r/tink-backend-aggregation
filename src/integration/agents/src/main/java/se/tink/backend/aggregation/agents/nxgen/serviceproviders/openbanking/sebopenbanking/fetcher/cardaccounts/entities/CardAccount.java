package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CardAccount {

    private List<Balance> balances;
    private CreditLimit creditLimit;
    private String currency;
    private String maskedPan;
    private String name;
    private String product;
    private String resourceId;
    private String status;
    private String usage;

    public List<Balance> getBalances() {
        return balances;
    }

    public CreditLimit getCreditLimit() {
        return creditLimit;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public String getName() {
        return name;
    }

    public String getProduct() {
        return product;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getStatus() {
        return status;
    }

    public String getUsage() {
        return usage;
    }

    @JsonIgnore
    public static CreditCardAccount toTinkTransaction(CardAccount cardAccount) {
        return CreditCardAccount.builder(cardAccount.createUniqueIdFromMaskedPane())
                .setAvailableCredit(cardAccount.getAvaliableCredit())
                .setBalance(cardAccount.getAvailableBalance())
                .setBankIdentifier(cardAccount.getResourceId())
                .setName(cardAccount.getProduct())
                .setAccountNumber(cardAccount.getName())
                .build();
    }

    @JsonIgnore
    private Amount getAvaliableCredit() {
        return new Amount(
                getCreditLimit().getCurrency(), getCreditLimit().getAmount());
    }

    @JsonIgnore
    private Amount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(Balance::isAvailableBalance)
                .findFirst()
                .map(Balance::toAmount)
                .orElse(BalancesEntity.getDefault());
    }

    @JsonIgnore
    private String createUniqueIdFromMaskedPane() {
        return maskedPan.split("[*]+")[1];
    }
}
