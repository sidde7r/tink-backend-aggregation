package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountEntity {

    private String iban;
    private String currency;
    private String name;

    private String resourceId;

    private String product;
    private String bic;
    private boolean cardAccount;
    private String interestRate;

    private String usage;

    private String status;

    @JsonProperty("balances")
    private List<BalanceEntity> balanceEntity;

    @JsonProperty("_links")
    private LinksEntity links;

    public boolean isEUR() {
        return QueryValues.EUR.equalsIgnoreCase(currency);
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? iban : name;
    }

    public Optional<TransactionalAccount> toTinkAccount(Optional<String> accountHolderName) {
        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(
                                LuminorConstants.ACCOUNT_TYPE_MAPPER,
                                product,
                                TransactionalAccountType.CHECKING)
                        .withBalance(getBalanceModule(balanceEntity))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(iban)
                                        .withAccountNumber(resourceId)
                                        .withAccountName(getName())
                                        .addIdentifier(new IbanIdentifier(iban))
                                        .build())
                        .setApiIdentifier(resourceId)
                        .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, iban);
        accountHolderName.ifPresent(builder::addHolderName);
        return builder.build();
    }

    public ExactCurrencyAmount getBalance() {
        return balanceEntity.stream()
                .findFirst()
                .map(BalanceEntity::toTinkAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep = BalanceModule.builder().withBalance(getBalance());
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BalanceMapper.getBookedBalance(balances);
        return balanceBuilderStep.build();
    }
}
