package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;
    private String bban;
    private String product;

    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        TransactionalAccountType type = getAccountType();
        switch (type) {
            case SAVINGS:
                return toSavingsAccount();
            case CHECKING:
                return toCheckingAccount();
            default:
                return Optional.empty();
        }
    }

    private Optional<TransactionalAccount> toSavingsAccount() {
        return toAccount(TransactionalAccountType.SAVINGS);
    }

    private Optional<TransactionalAccount> toCheckingAccount() {
        return toAccount(TransactionalAccountType.CHECKING);
    }

    private Optional<TransactionalAccount> toAccount(TransactionalAccountType type) {
        return Optional.of(
                TransactionalAccount.nxBuilder()
                        .withType(type)
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(bban)
                                        .withAccountNumber(getAccountNumber())
                                        .withAccountName(Optional.ofNullable(name).orElse(""))
                                        .addIdentifier(getIdentifier())
                                        .addIdentifier(AccountIdentifier.create(Type.NO, bban))
                                        .build())
                        .withBalance(BalanceModule.of(getBalance()))
                        .setApiIdentifier(resourceId)
                        .setBankIdentifier(resourceId)
                        .addHolderName(Optional.ofNullable(name).orElse(""))
                        .putInTemporaryStorage(
                                SparebankConstants.StorageKeys.TRANSACTIONS_URL,
                                getTransactionLink())
                        .build());
    }

    protected ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(this::doesMatchWithAccountCurrency)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElse(getDefaultAmount());
    }

    protected String getUniqueIdentifier() {
        return iban;
    }

    protected String getAccountNumber() {
        return iban;
    }

    protected AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    protected List<BalanceEntity> getBalances() {
        return balances;
    }

    protected boolean doesMatchWithAccountCurrency(final BalanceEntity balance) {
        return !balance.isClosingBooked() && balance.isInCurrency(currency);
    }

    protected ExactCurrencyAmount getDefaultAmount() {
        return ExactCurrencyAmount.of(0, currency);
    }

    protected String getTransactionLink() {
        return Optional.ofNullable(links).map(AccountLinksEntity::getTransactionLink).orElse("");
    }

    private TransactionalAccountType getAccountType() {
        return SparebankConstants.ACCOUNT_TYPE_MAPPER
                .translate(Optional.ofNullable(cashAccountType).orElse(product))
                .orElse(TransactionalAccountType.OTHER);
    }
}
