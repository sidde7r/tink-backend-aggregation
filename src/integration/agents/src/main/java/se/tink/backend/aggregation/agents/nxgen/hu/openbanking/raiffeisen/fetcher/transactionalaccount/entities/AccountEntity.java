package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<BalanceEntity> balances;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String name;
    private String product;
    private String resourceId;
    private String status;

    public Boolean isCheckingAccount() {
        return ACCOUNT_TYPE_MAPPER
                .translate(cashAccountType)
                .orElse(TransactionalAccountType.OTHER)
                .equals(TransactionalAccountType.CHECKING);
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setBankIdentifier(resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance() {
        return Optional.ofNullable(balances)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .orElse(BalanceEntity.Default);
    }

    private TransactionalAccountType getAccountType() {
        return ACCOUNT_TYPE_MAPPER.translate(cashAccountType).orElse(null);
    }
}
