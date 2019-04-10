package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<String> accountOwners;
    private List<BalanceEntity> balances;
    private String bic;
    private String currency;
    private String iban;
    private String name;
    private String product;
    private String resourceId;

    public boolean isCheckingAccount() {
        return AktiaConstants.ACCOUNT_TYPE_MAPPER
                .translate(product)
                .orElse(AccountTypes.OTHER)
                .equals(AccountTypes.CHECKING);
    }

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getAmount())
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(getOwners())
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getAmount() {
        return balances.stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .get()
                .getAmount();
    }

    private String getOwners() {
        return String.join(", ", accountOwners);
    }
}
