package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String name;
    private String currency;
    @JsonProperty("_links")
    private LinksEntity links;

    public String getResourceId() {
        return resourceId;
    }

    public String getBalancesUrl() {
        return links.getBalancesUrl();
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionalAccount toTinkAccount(Amount balance) {
        return CheckingAccount.builder()
            .setUniqueIdentifier(iban)
            .setAccountNumber(iban)
            .setBalance(balance)
            .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
            .addHolderName(name)
            .setAlias(name)
            .setApiIdentifier(resourceId)
            .putInTemporaryStorage(IngConstants.StorageKeys.ACCOUNT_ID, resourceId)
            .putInTemporaryStorage(IngConstants.StorageKeys.TRANSACTIONS_URL,
                links.getTransactionsUrl())
            .build();
    }
}