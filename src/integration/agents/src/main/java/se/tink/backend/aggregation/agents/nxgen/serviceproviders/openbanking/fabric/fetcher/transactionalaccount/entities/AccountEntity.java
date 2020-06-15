package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String bban;
    private String iban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String details;
    private String linkedAccounts;
    private String msisdn;
    private String name;
    private String product;
    private String resourceId;

    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private LinksEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        FabricConstants.ACCOUNT_TYPE_MAPPER,
                        Optional.ofNullable(cashAccountType).orElse(Accounts.CASH),
                        TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(getIbanIdentifier())
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .build();
    }

    public ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isClosingBooked)
                .findFirst()
                .map(BalanceEntity::toAmonut)
                .orElseThrow(() -> new IllegalStateException("No amount found"));
    }

    private AccountIdentifier getIbanIdentifier() {
        return new IbanIdentifier(iban);
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }

    public String getBalancesLink() {
        return links.getBalnacesLink();
    }

    public String getAccountDetialsLink() {
        return links.getAccountDetailsLink();
    }
}
