package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountEntity {

    @Getter private String iban;
    private String currency;
    private String name;

    // AccountId
    @Getter private String resourceId;

    // Current account, Payment card account, Minimum salary account, Investment account
    private String product;
    private String bic;
    private boolean cardAccount;
    private String interestRate;

    // PRIV / ORGA
    private String usage;

    // Enabled / blocked
    private String status;

    @JsonProperty("balances")
    private List<BalanceEntity> balanceEntity;

    @JsonProperty("_links")
    LinksEntity links;

    private boolean isPrivateAccount() {
        return "PRIV".equalsIgnoreCase(usage);
    }

    private String getName() {
        return Strings.isNullOrEmpty(name) ? iban : name;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        LuminorConstants.ACCOUNT_TYPE_MAPPER,
                        product,
                        TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(balanceEntity.get(0).balanceAmountEntity.toAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(resourceId)
                                .withAccountName(getName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setBankIdentifier(resourceId)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(LuminorConstants.StorageKeys.ACCOUNT_ID, iban)
                .build();
    }
}
