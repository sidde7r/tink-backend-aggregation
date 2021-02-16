package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlinGroupBalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String product;
    private String iban;
    private String currency;
    private String status;
    private String name;
    private String cashAccountType;
    private String ownerName;
    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private AccountLinksWithHrefEntity links;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        DeutscheBankConstants.ACCOUNT_TYPE_MAPPER,
                        Optional.ofNullable(cashAccountType).orElse(product),
                        TransactionalAccountType.CHECKING)
                .withBalance(getBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(getIdentifier())
                                .build())
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getUniqueIdentifier())
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .addHolderName(ownerName)
                .build();
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder()
                        .withBalance(BerlinGroupBalanceMapper.getBookedBalance(balances));
        BerlinGroupBalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BerlinGroupBalanceMapper.getCreditLimit(balances)
                .ifPresent(balanceBuilderStep::setCreditLimit);
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

    private String getTransactionLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getTransactionLink)
                .orElse("");
    }

    private String getUniqueIdentifier() {
        return iban;
    }

    private AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }

    private String getAccountNumber() {
        return iban;
    }

    private String getAccountName() {
        return Stream.of(name, cashAccountType, product)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
