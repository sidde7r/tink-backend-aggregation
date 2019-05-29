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
public class CardAccountEntity {

    private List<BalanceEntity> balances;
    private CreditLimitEntity creditLimit;
    private String currency;
    private String maskedPan;
    private String name;
    private String product;
    private String resourceId;
    private String status;
    private String usage;

    public List<BalanceEntity> getBalances() {
        return balances;
    }

    public CreditLimitEntity getCreditLimit() {
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
    public static CreditCardAccount toTinkTransaction(CardAccountEntity cardAccount) {
        return CreditCardAccount.builder(cardAccount.createUniqueIdFromMaskedPane())
                .setAvailableCredit(cardAccount.getAvaliableCredit())
                .setBalance(cardAccount.getAvailableBalance())
                .setBankIdentifier(cardAccount.getResourceId())
                .setName(cardAccount.getProduct())
                .setAccountNumber(cardAccount.getMaskedPan())
                .build();
    }

    @JsonIgnore
    private Amount getAvaliableCredit() {
        return new Amount(getCreditLimit().getCurrency(), getCreditLimit().getAmount());
    }

    @JsonIgnore
    private Amount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElse(BalancesEntity.getDefault());
    }

    @JsonIgnore
    private String createUniqueIdFromMaskedPane() {
        return maskedPan.split("[*]+")[1];
    }
}
