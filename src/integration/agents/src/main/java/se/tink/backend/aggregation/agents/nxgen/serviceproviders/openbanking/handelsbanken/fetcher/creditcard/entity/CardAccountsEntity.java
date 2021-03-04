package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.AccountBalance;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardAccountsEntity {
    private String accountId;
    private List<CardsEntity> cards;
    private String currency;
    private CreditLimitEntity creditLimit;
    private List<BalancesEntity> balances;
    private String product;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(getCardDetails())
                .withoutFlags()
                .withId(getIdModule())
                .addHolderName(getHolderName())
                .setApiIdentifier(accountId)
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(cards.get(0).getMaskedPan())
                .withAccountNumber(cards.get(0).getMaskedPan())
                .withAccountName(product)
                .addIdentifier(
                        AccountIdentifier.create(
                                Type.PAYMENT_CARD_NUMBER, cards.get(0).getMaskedPan()))
                .build();
    }

    private CreditCardModule getCardDetails() {
        final ExactCurrencyAmount availableCredit = getAvailableCredit();
        return CreditCardModule.builder()
                .withCardNumber(cards.get(0).getMaskedPan())
                .withBalance(getAvailableBalance(availableCredit).negate())
                .withAvailableCredit(availableCredit)
                .withCardAlias(product)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance(ExactCurrencyAmount availableCredit) {

        return creditLimit != null
                ? ExactCurrencyAmount.of(
                        creditLimit.getAmount().subtract(availableCredit.getExactValue()),
                        creditLimit.getCurrency())
                : ExactCurrencyAmount.of(
                        BigDecimal.ZERO,
                        Optional.ofNullable(currency).orElseGet(availableCredit::getCurrencyCode));
    }

    private ExactCurrencyAmount getAvailableCredit() {
        BalancesEntity availableBalance =
                balances.stream()
                        .filter(
                                balance ->
                                        balance.getBalanceType()
                                                .contains(AccountBalance.AVAILABLE_BALANCE))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException("Could not get available credit"));

        return ExactCurrencyAmount.of(
                availableBalance.getBalanceAmount().getAmount(),
                availableBalance.getBalanceAmount().getCurrency());
    }

    public String getHolderName() {
        return cards.stream().map(CardsEntity::getName).findFirst().orElse(null);
    }
}
