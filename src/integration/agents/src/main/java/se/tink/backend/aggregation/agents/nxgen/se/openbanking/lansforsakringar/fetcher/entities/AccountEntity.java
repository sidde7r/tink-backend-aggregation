package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<String> pan;
    private List<String> allowedTransactionTypes;
    private List<BalanceEntity> balances;
    private String bban;
    private String currency;
    private String href;
    private String name;
    private String product;
    private String resourceId;

    private String getPan() {
        return pan.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pan found in the response"))
                .replace("*", "");
    }

    private BalanceEntity getBalance() {
        return balances.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }

    @JsonIgnore
    public boolean isCreditCardAccount() {
        return "Kreditkort Privat".equalsIgnoreCase(product);
    }

    @JsonIgnore
    public boolean isNotCreditCardAccount() {
        return !(isCreditCardAccount());
    }

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                // There is no clearing number
                .setUniqueIdentifier(bban)
                .setAccountNumber(bban)
                .setBalance(getAvailableBalance())
                .setAlias(getName())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, bban))
                .setProductName(product)
                .setApiIdentifier(resourceId)
                .build();
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? bban : name;
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElse(BalanceEntity.Default);
    }

    public CreditCardAccount toTinkCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(getPan())
                                .withBalance(ExactCurrencyAmount.of(0d, currency))
                                .withAvailableCredit(getBalance().getBalanceAmount().getAmount())
                                .withCardAlias(getName())
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getPan())
                                .withAccountNumber(getPan())
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.BBAN, bban))
                                .setProductName(product)
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }
}
