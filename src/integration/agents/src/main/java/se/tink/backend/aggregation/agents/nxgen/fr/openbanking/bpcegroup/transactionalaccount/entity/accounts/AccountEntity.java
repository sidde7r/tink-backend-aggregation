package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String cashAccountType;
    private AccountId accountId;
    private String resourceId;
    private String product;

    @JsonProperty("_links")
    private NavigationLinksEntity links;

    private String usage;
    private String psuStatus;
    private String name;
    private String bicFi;
    private String currency;
    private String details;
    private List<BalanceEntity> balances;
    private String linkedAccount;

    public Optional<TransactionalAccount> toTinkAccount() {
        final String iban = accountId.getIban();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(bicFi)
                .build();
    }

    private String getAccountNumber() {
        final String iban = accountId.getIban();
        return iban.substring(iban.length() - 23);
    }

    private ExactCurrencyAmount getBalance() {
        return getBalances().stream()
                .filter(BalanceEntity::isBalanceType)
                .map(BalanceEntity::getTinkAmount)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No balance found"));
    }

    public List<BalanceEntity> getBalances() {
        return Optional.ofNullable(balances).orElseGet(Collections::emptyList);
    }

    public boolean isTransactionalAccount() {
        return AccountType.TRANSACTIONAL.getType().equalsIgnoreCase(cashAccountType);
    }

    public String getIban() {
        return accountId.getIban();
    }
}
