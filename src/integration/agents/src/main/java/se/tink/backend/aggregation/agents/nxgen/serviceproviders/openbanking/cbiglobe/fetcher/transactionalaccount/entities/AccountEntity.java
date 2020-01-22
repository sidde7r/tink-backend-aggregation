package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@NoArgsConstructor
public class AccountEntity {
    @JsonProperty("_links")
    private LinksEntity links;

    private String currency;
    private String href;
    private String bban;
    private String iban;
    private String resourceId;
    private String name;
    private String product;

    public AccountEntity(String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Optional<TransactionalAccount> toTinkAccount(GetBalancesResponse getBalancesResponse) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance(getBalancesResponse.getBalances())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getName())
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    private ExactCurrencyAmount getBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .min(Comparator.comparing(BalanceEntity::getBalanceMappingPriority))
                .map(BalanceEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.BALANCE_NOT_FOUND));
    }

    private String getName() {
        return Optional.ofNullable(name).orElse(iban);
    }

    private String getAccountNumber() {
        return Optional.ofNullable(bban).orElse(iban);
    }
}
