package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Locale;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String maskedPan;
    private String name;
    private String currency;
    private String product;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getResourceId() {
        return resourceId;
    }

    @JsonIgnore
    public String getBalancesUrl() {
        return links.getBalancesUrl();
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return Strings.nullToEmpty(maskedPan).isEmpty();
    }

    @JsonIgnore
    public boolean isCardAccount() {
        return !Strings.nullToEmpty(maskedPan).isEmpty();
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(
            ExactCurrencyAmount balance, boolean lowercaseAccountId) {
        if (!isTransactionalAccount()) {
            throw new IllegalStateException("Not a transactional account.");
        }
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balance))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier(lowercaseAccountId))
                                .withAccountNumber(iban)
                                .withAccountName(product)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(iban)
                .putInTemporaryStorage(IngBaseConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .putInTemporaryStorage(
                        IngBaseConstants.StorageKeys.TRANSACTIONS_URL, links.getTransactionsUrl())
                .build();
    }

    @JsonIgnore
    public String getUniqueIdentifier(boolean lowercase) {
        if (lowercase) {
            return iban.toLowerCase(Locale.ROOT);
        }
        return iban.toUpperCase(Locale.ROOT);
    }
}
