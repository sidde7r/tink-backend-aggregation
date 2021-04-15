package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.BalanceTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.AccountTypeMapperBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
@Data
public class AccountEntity {
    @JsonProperty("_links")
    private LinksEntity links;

    private AccountIdEntity accountId;
    private List<BalanceEntity> balances;
    private String bicFi;
    private CashAccountTypeEntity cashAccountType;
    private String currency;
    private String details;
    private String linkedAccount;
    private String name;
    private String product;
    private String psuStatus;
    private String resourceId;
    private String usage;

    public boolean areConsentsNecessary() {
        return resourceId == null
                || links == null
                || !links.hasBalances()
                || !links.hasTransactions();
    }

    public AccountIdEntity getAccountId() {
        return accountId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        final String iban = Optional.ofNullable(accountId.getIban()).orElse(resourceId);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        AccountTypeMapperBuilder.build(),
                        cashAccountType.toString(),
                        TransactionalAccountType.OTHER)
                .withBalance(getBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    public CreditCardAccount convertToCreditCards() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountId.getOther().getCardNumber())
                                .withBalance(getBalanceCreditCards())
                                .withAvailableCredit(getAvailableCredits())
                                .withCardAlias(product)
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(resourceId)
                                .withAccountNumber(linkedAccount)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                accountId.getOther().getCardNumber()))
                                .setProductName(product)
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance());
        getAvailableBalance().ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance() {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }

        Optional<BalanceEntity> balanceEntity =
                balances.stream()
                        .filter(b -> BalanceTypes.CLBD.equalsIgnoreCase(b.getBalanceType()))
                        .findAny();

        if (balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }

        return balanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toAmount)
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance() {
        return balances.stream()
                .filter(b -> BalanceTypes.XPCD.equalsIgnoreCase(b.getBalanceType()))
                .findAny()
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toAmount);
    }

    private ExactCurrencyAmount getBalanceCreditCards() {
        return balances != null && !balances.isEmpty()
                ? balances.get(balances.size() - 1).getBalanceAmount().toAmount()
                : null;
    }

    private ExactCurrencyAmount getAvailableCredits() {
        return ExactCurrencyAmount.of(BigDecimal.ZERO, "EUR");
    }
}
