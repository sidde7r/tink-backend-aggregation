package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountEntity {
    private String iban;
    private String cashAccountType;
    private String name;
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
                .withBalance(FabricBalanceTransform.calculate(balances))
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

    private AccountIdentifier getIbanIdentifier() {
        return new IbanIdentifier(iban);
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }

    public String getBalancesLink() {
        return links.getBalnacesLink();
    }

    public String getAccountDetailsLink() {
        return links.getAccountDetailsLink();
    }
}
