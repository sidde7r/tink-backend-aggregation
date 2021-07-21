package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

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
                                .addIdentifier(getIdentifier())
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(ownerName)
                .build();
    }

    private BalanceModule getBalanceModule() {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        return balanceBuilderStep.build();
    }

    private String getAccountName() {
        return Stream.of(name, product, iban).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private AccountIdentifier getIdentifier() {
        return new IbanIdentifier(iban);
    }
}
