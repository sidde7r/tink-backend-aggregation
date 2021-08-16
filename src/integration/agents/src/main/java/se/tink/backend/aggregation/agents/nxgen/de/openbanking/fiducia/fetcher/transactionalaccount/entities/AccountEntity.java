package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountEntity {

    private String iban;
    private String resourceId;
    private List<BalanceEntity> balances;
    private String ownerName;

    public String getResourceId() {
        return resourceId;
    }

    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(getBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(iban)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(ownerName)
                .build();
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BalanceMapper.getCreditLimit(balances).ifPresent(balanceBuilderStep::setCreditLimit);
        return balanceBuilderStep.build();
    }
}
