package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String bban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String details;
    private String displayName;
    private String iban;
    private String linkedAccounts;
    private String msisdn;
    private String name;
    private String ownerName;
    private String product;
    private String resourceId;
    private String status;
    private String usage;
    private List<BalanceEntity> balances;

    public Optional<TransactionalAccount> toTinkAccount(List<BalanceEntity> accountBalances) {
        this.balances = accountBalances;
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        CitadeleBaseConstants.ACCOUNT_TYPE_MAPPER,
                        usage,
                        TransactionalAccountType.CHECKING)
                .withBalance(getBalanceModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(getAccountName())
                                .addIdentifier(new IbanIdentifier(bic, iban))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(ownerName)
                .build();
    }

    public ExactCurrencyAmount getBookedBalance() {
        return balances.stream()
                .filter(BalanceEntity::isBooked)
                .findFirst()
                .map(balance -> balance.getBalanceAmount().toTinkAmount())
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }

    public Optional<ExactCurrencyAmount> getAvailableBalance() {
        return balances.stream()
                .filter(BalanceEntity::isAvailable)
                .findFirst()
                .map(balance -> balance.getBalanceAmount().toTinkAmount());
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance());
        getAvailableBalance().ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private String getAccountName() {
        return Stream.of(name, product, iban).filter(Objects::nonNull).findFirst().orElse(null);
    }
}
