package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
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
    private String resourceId;
    private String product;
    private String bban;

    @JsonProperty("_links")
    private LinksEntity links;

    private String iban;
    private String currency;
    private List<BalancesEntity> balances;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(bban)
                                .withAccountName(product)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(
                        ArgentaConstants.StorageKeys.TRANSACTIONS_URL,
                        links.getTransactions().getHref())
                .build();
    }

    private ExactCurrencyAmount getBalance() {

        return balances.stream()
                .filter(BalancesEntity::isInterimAvailable)
                .findFirst()
                .orElseGet(() -> balances.stream().findAny().orElseGet(BalancesEntity::new))
                .getBalanceAmount();
    }
}
