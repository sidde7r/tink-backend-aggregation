package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private String bban;
    private String bic;
    private String currency;
    private String iban;
    private String product;
    private String resourceId;

    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private LinksEntity links;

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(
                        FidorConstants.ACCOUNT_TYPE_MAPPER
                                .translate(product)
                                .orElse(TransactionalAccountType.OTHER))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName("")
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .setApiIdentifier(iban)
                .setBankIdentifier(iban)
                .build();
    }

    private Amount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException("Balance is not available"));
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
