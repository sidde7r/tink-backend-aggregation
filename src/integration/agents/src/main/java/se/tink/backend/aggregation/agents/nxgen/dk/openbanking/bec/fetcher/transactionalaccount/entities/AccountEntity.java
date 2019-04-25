package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities;

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

    private String bic;
    private String currency;
    private String iban;
    private String product;
    private String resourceId;
    private List<BalanceEntity> balances;

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(bic)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setProductName(product)
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailable)
                .findFirst()
                .map(BalanceEntity::getAmount)
                .orElse(BalanceEntity.Default);
    }
}
