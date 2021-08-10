package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountEntity {
    private String iban;
    private String name;
    private String resourceId;
    private List<BalanceEntity> balances;
    private String ownerName;
    private String bic;

    private static final Pattern OWNER_DELIMETER_PATTERN = Pattern.compile("\\s(?i)und\\s");

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(bic, iban))
                                .addIdentifier(new BbanIdentifier(iban.substring(4)))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .addParties(parseOwnername(ownerName))
                .build();
    }

    private List<Party> parseOwnername(String ownerName) {
        return OWNER_DELIMETER_PATTERN
                .splitAsStream(ownerName)
                .map(owner -> new Party(owner.trim(), Role.HOLDER))
                .collect(Collectors.toList());
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BalanceMapper.getCreditLimit(balances).ifPresent(balanceBuilderStep::setCreditLimit);
        return balanceBuilderStep.build();
    }

    public String getResourceId() {
        return resourceId;
    }

    public List<BalanceEntity> getBalances() {
        return balances;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
