package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
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

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    public CreditLimit getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(CreditLimit creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
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
                getCreditLimit().getCurrency(), BigDecimal.valueOf(getCreditLimit().getAmount()));
    }

    @JsonIgnore
    private Amount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(Balance::isAvailableBalance)
                .findFirst()
                .map(Balance::toAmount)
                .orElse(BalancesEntity.Default);
    }

    @JsonIgnore
    private String createUniqueIdFromMaskedPane() {
        return maskedPan.split("[*]+")[1];
    }
}
