package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsItemEntity {

    private String cashAccountType;

    private String resourceId;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private String usage;

    private String psuStatus;

    private String name;

    private String bicFi;

    private String currency;

    public String getCashAccountType() {
        return cashAccountType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public String getUsage() {
        return usage;
    }

    public String getPsuStatus() {
        return psuStatus;
    }

    public String getName() {
        return name;
    }

    public String getBicFi() {
        return bicFi;
    }

    public String getCurrency() {
        return currency;
    }

    // TODO check if checking is correct
    private TransactionalAccountType getAccountType() {
        if (cashAccountType.equalsIgnoreCase("cacc")) {
            return TransactionalAccountType.from(AccountTypes.CHECKING);
        } else {
            return TransactionalAccountType.from(AccountTypes.CHECKING);
        }
    }

    public TransactionalAccount toTinkModel(BalanceResponse balanceResponse) {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withBalance(BalanceModule.of(getAvailableBalance(balanceResponse)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(resourceId)
                                .withAccountNumber(resourceId)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(resourceId))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage("ACCOUNT_ID", resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance(BalanceResponse balanceResponse) {

        return Optional.ofNullable(balanceResponse.getBalances()).orElse(Collections.emptyList())
                .stream()
                .filter(BalancesItemEntity::isAvailableBalance)
                .findFirst()
                .map(BalancesItemEntity::toAmount)
                .orElseThrow(IllegalStateException::new);
    }
}
