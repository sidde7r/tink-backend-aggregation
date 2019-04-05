package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants;
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
        return RaiffeisenConstants.ACCOUNT_TYPE_MAPPER
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
        return balances != null && !balances.isEmpty()
                ? balances.stream()
                        .filter(BalanceEntity::isAvailableBalance)
                        .findFirst()
                        .orElse(balances.get(0))
                        .toAmount()
                : BalanceEntity.Default;
    }
}
