package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.account;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String bban;
    private String currency;
    private String name;
    private String cashAccountType;
    private String bic;
    private String usage;
    private LinksEntity links;

    public String getResourceId() {
        return resourceId;
    }

    public Optional<TransactionalAccount> toTinkAccount(ExactCurrencyAmount balanceAmount) {
        return TransactionalAccount.nxBuilder()
                .withType(accountType())
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balanceAmount))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(resourceId)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .build();
    }

    private TransactionalAccountType accountType() {
        return StringUtils.containsIgnoreCase(name, "spare")
                ? TransactionalAccountType.SAVINGS
                : TransactionalAccountType.CHECKING;
    }
}
