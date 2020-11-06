package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<String> pan;

    private List<String> allowedTransactionTypes;
    private String href;
    private List<BalanceEntity> balances;
    private String bban;
    private String currency;
    private String name;
    private String product;
    private String resourceId;

    @JsonIgnore
    public LinksEntity getLinks() {
        return links;
    }

    public List<String> getAllowedTransactionTypes() {
        return allowedTransactionTypes;
    }

    public String getHref() {
        return href;
    }

    private String getPan() {
        return pan.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pan found in the response"))
                .replace("*", "");
    }

    private ExactCurrencyAmount getBalance() {
        return balances.stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::getBalanceAmount)
                .map(BalanceAmountEntity::getAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? bban : name;
    }

    @JsonIgnore
    public boolean isCreditCardAccount() {
        return pan != null;
    }

    @JsonIgnore
    public boolean isNotCreditCardAccount() {
        return !(isCreditCardAccount());
    }

    @JsonIgnore
    private TransactionalAccountType getAccountTyoe() {
        return LansforsakringarConstants.ACCOUNT_TYPE_MAPPER
                .translate(product)
                .orElseThrow(() -> new IllegalStateException("Could not translate account type"));
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {

        return TransactionalAccount.nxBuilder()
                .withType(getAccountTyoe())
                .withoutFlags()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(bban)
                                .withAccountName(getName())
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.SE, bban))
                                .setProductName(product)
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    public CreditCardAccount toTinkCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(getPan())
                                .withBalance(ExactCurrencyAmount.zero(currency))
                                .withAvailableCredit(getBalance())
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
