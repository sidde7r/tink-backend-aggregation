package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<String> allowedTransactionTypes;
    private List<BalanceEntity> balances;
    private String bban;
    private String currency;
    private String href;
    private String name;
    private String product;
    private String resourceId;

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
}
