package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class AccountEntity {

    @Getter private String resourceId;

    @Getter private String iban;

    @Getter private String bban;

    private String currency;

    private String name;

    private String product;

    @JsonProperty("_links")
    private AccountsLinksEntity links;

    @JsonIgnore private TransactionalAccountType accountType;

    @JsonIgnore private String accountId;

    @JsonIgnore private AccountIdentifierType accountIdentifierType;

    public Optional<TransactionalAccount> toTinkAccount(List<BalanceEntity> balances) {
        setUpAccountTypeRelevantProperties();

        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId)
                                .withAccountNumber(accountId)
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                accountIdentifierType, accountId, null))
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .setBankIdentifier(accountId)
                .build();
    }

    private void setUpAccountTypeRelevantProperties() {
        if (bban != null) {
            accountType = TransactionalAccountType.SAVINGS;
            accountId = bban;
            accountIdentifierType = AccountIdentifierType.BBAN;
        } else {
            accountType = TransactionalAccountType.CHECKING;
            accountId = iban;
            accountIdentifierType = AccountIdentifierType.IBAN;
        }
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }
}
