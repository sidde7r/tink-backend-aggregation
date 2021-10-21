package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.utils.VolksbankUtils;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountsEntity {

    @Getter private String resourceId;
    private String product;
    private String iban;
    private String name;
    @Getter private String ownerName;
    private String currency;
    private String customerBic;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(List<BalanceEntity> balances) {
        // Account type is not present, parse every account as checking account
        return toCheckingAccount(balances);
    }

    @JsonIgnore
    private Optional<TransactionalAccount> toCheckingAccount(List<BalanceEntity> balances) {

        balances.sort((o1, o2) -> o2.getLastChangeDateTime().compareTo(o1.getLastChangeDateTime()));

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(getBalanceModule(balances))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(iban)
                                        .withAccountNumber(VolksbankUtils.getAccountNumber(iban))
                                        .withAccountName(name)
                                        .addIdentifier(new IbanIdentifier(iban))
                                        .build())
                        .setApiIdentifier(getResourceId());

        addHolderNamesToBuilder(builder);

        return builder.build();
    }

    @JsonIgnore
    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        return balanceBuilderStep.build();
    }

    @JsonIgnore
    private void addHolderNamesToBuilder(TransactionalBuildStep builder) {
        if (Strings.isNullOrEmpty(ownerName)) {
            return;
        }
        Stream.of(ownerName.split(" CJ ")).forEach(builder::addHolderName);
    }
}
