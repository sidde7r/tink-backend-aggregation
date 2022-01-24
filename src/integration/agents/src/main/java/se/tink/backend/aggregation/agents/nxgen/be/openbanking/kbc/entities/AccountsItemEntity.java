package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountsItemEntity {

    private String cashAccountType;
    private List<BalancesItemEntity> balances;
    private String product;
    private String resourceId;
    private String iban;
    private String name;
    private String currency;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        KbcConstants.ACCOUNT_TYPE_MAPPER,
                        cashAccountType,
                        TransactionalAccountType.CHECKING)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(product)
                                .addIdentifiers(accountIdentifiers())
                                .build())
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(KbcConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance() {

        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .findFirst()
                .map(BalancesItemEntity::getBalanceAmountEntity)
                .orElseThrow(() -> new IllegalStateException("Could not get balance"));
    }

    private List<AccountIdentifier> accountIdentifiers() {
        return Arrays.asList(new SepaEurIdentifier(iban), new IbanIdentifier(iban));
    }
}
