package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountsItem {

    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("_links")
    private Links links;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("status")
    private String status;

    @JsonAlias("name")
    private String ownerName;

    public String getResourceId() {
        return resourceId;
    }

    public String getIban() {
        return iban;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toCheckingAccount(final BalanceResponse balanceResponse) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balanceResponse.toAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(iban)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(ownerName)
                .putInTemporaryStorage(StorageKey.RESOURCE_ID, getResourceId())
                .build();
    }
}
