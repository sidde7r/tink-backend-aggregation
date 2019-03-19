package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
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
                .setUniqueIdentifier(resourceId)
                .setAccountNumber(bban)
                .setBalance(getAvailableBalance())
                .addAccountIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.SE, bban))
                .setAlias(getName())
                .setProductName(product)
                .setApiIdentifier(resourceId)
                .build();
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? bban : name;
    }

    private Amount getAvailableBalance() {
        return balances != null
                ? balances.stream()
                        .filter(BalanceEntity::isAvailableBalance)
                        .findFirst()
                        .orElse(new BalanceEntity())
                        .toAmount()
                : BalanceEntity.Default;
    }
}
