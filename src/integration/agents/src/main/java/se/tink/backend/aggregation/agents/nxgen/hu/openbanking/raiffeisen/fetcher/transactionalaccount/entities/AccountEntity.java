package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.AccountTypes;
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
    private String cashAccountType;
    private String currency;
    private String iban;
    private String name;
    private String product;
    private String resourceId;
    private String status;

    public Boolean isCheckingAccount() {
        return ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .orElse(AccountTypes.OTHER)
                .equals(AccountTypes.CHECKING);
    }

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getAvailableBalance())
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setApiIdentifier(resourceId)
                .setProductName(product)
                .build();
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElse(BalanceEntity.Default);
    }
}
