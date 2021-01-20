package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.AccountTypeMapperBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
        return resourceId != null
                && (links == null || !links.hasBalances() || !links.hasTransactions());
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
                .withBalance(BalanceModule.of(getAvailableBalance()))
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
                                                Type.PAYMENT_CARD_NUMBER,
                                                accountId.getOther().getCardNumber()))
                                .setProductName(product)
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailablebalance)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.INVALID_BALANCE_TYPE))
                .toAmount();
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
