package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
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
public class AccountsItemEntity {

    private CashAccountTypeEntity cashAccountType;

    @JsonProperty("accountId")
    private AccountIdEntity accountIdEntity;

    private String resourceId;

    private List<BalancesItemEntity> balances;

    @JsonProperty("_links")
    private Href links;

    private String usage;

    private String psuStatus;

    private String name;

    private String linkedAccount;

    private String bicFi;

    public String getResourceId() {
        return resourceId;
    }

    public CashAccountTypeEntity getCashAccountType() {
        return cashAccountType;
    }

    public Optional<TransactionalAccount> toTinkModel(String connectedPsu) {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountIdEntity.getIban())
                                .withAccountNumber(accountIdEntity.getIban())
                                .withAccountName(getAccountName())
                                .addIdentifier(new IbanIdentifier(accountIdEntity.getIban()))
                                .build())
                .addHolderName(connectedPsu)
                .setApiIdentifier(resourceId)
                .build();
    }

    public CreditCardAccount toTinkCreditCard() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountIdEntity.getOther().getIdentification())
                                .withBalance(getBalance())
                                .withAvailableCredit(ExactCurrencyAmount.of(0, "EUR"))
                                .withCardAlias(name)
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
                                                accountIdEntity.getOther().getIdentification()))
                                .setProductName(name)
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private TransactionalAccountType getAccountType() {
        return CashAccountTypeEntity.CACC != cashAccountType
                ? TransactionalAccountType.SAVINGS
                : TransactionalAccountType.CHECKING;
    }

    private String getAccountName() {
        if (!Strings.isNullOrEmpty(name)) {
            return name;
        } else {
            return cashAccountType.toString();
        }
    }

    private ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .findFirst()
                .map(BalancesItemEntity::getBalanceAmount)
                .map(BalanceAmountEntity::getAmount)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SocieteGeneraleConstants.ErrorMessages.MISSING_BALANCE));
    }

    public boolean isCreditCard() {
        return CashAccountTypeEntity.CARD == cashAccountType;
    }
}
