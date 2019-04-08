package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    public TransactionalAccount toTinkAccount() {
        Optional<AccountTypes> accountType = AktiaConstants.ACCOUNT_TYPE_MAPPER.translate(product);
        if (!accountType.isPresent()) {
            throw new IllegalStateException("Unknown account type.");
        }

        if (accountType.get().equals(AccountTypes.CHECKING)) {
            return parseCheckingAccount();
        }

        throw new IllegalStateException("Unknown account type.");
    }

    private TransactionalAccount parseCheckingAccount() {
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
