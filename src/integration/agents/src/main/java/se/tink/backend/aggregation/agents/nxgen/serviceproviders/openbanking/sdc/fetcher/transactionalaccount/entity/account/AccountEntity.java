package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.account;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
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
    private String ownerName;
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
                                .withAccountNumber(bban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(bic, iban))
                                .addIdentifier(new BbanIdentifier(bban))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .addParties(getParties(ownerName))
                .setHolderType(AccountHolderType.PERSONAL)
                .build();
    }

    private TransactionalAccountType accountType() {
        return StringUtils.containsIgnoreCase(name, "spare")
                ? TransactionalAccountType.SAVINGS
                : TransactionalAccountType.CHECKING;
    }

    private List<Party> getParties(String ownerName) {
        if (ownerName != null) {
            String[] owners = StringUtils.split(ownerName, ",");
            return Arrays.stream(owners)
                    .map(owner -> new Party(owner.trim(), Party.Role.UNKNOWN))
                    .collect(Collectors.toList());
        }
        return Collections.singletonList(new Party("", Party.Role.UNKNOWN));
    }
}
