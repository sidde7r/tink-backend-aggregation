package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    @JsonProperty("_links")
    private LinksEntity links;

    private List<BalanceEntity> balances;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String details;
    private String iban;
    private String linkedAccounts;
    private String name;
    private String product;
    private String resourceId;
    private String status;
    private String usage;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        // replace function is used because mocked date is not valid
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban.substring(4).replace(" ", ""))
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(name)
                .addAccountIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.IBAN, iban.replace(" ", "")))
                .setProductName(product)
                .setApiIdentifier(resourceId.replace(" ", "%20"))
                .build();
    }

    @JsonIgnore
    private Amount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailable)
                .findFirst()
                .map(BalanceEntity::getAmount)
                .orElse(BalanceEntity.Default);
    }
}
