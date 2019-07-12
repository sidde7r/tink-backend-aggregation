package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
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

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(AccountTypes.CHECKING))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .withBalance(BalanceModule.of(balance))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(iban)
                .putInTemporaryStorage(IngConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .putInTemporaryStorage(
                        IngConstants.StorageKeys.TRANSACTIONS_URL, links.getTransactionsUrl())
                .build();
    }
}
