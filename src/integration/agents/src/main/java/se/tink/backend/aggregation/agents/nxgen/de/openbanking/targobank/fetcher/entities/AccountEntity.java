package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private String name;
    private String cashAccountType;
    private String status;
    private String bic;
    private String usage;
    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(
                        TargobankConstants.ACCOUNT_TYPE_MAPPER
                                .translate(cashAccountType)
                                .orElse(TransactionalAccountType.OTHER))
                .withBalance(BalanceModule.of(getBallanceAmmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(resourceId)
                                .withAccountName(Optional.ofNullable(name).orElse(""))
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(resourceId)
                .addHolderName(Optional.ofNullable(name).orElse(""))
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getBallanceAmmount() {
        BalanceAmountEntity balanceAmount =
                balances.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(""))
                        .getBalanceAmount();
        return new ExactCurrencyAmount(
                new BigDecimal(balanceAmount.getAmount()), balanceAmount.getCurrency());
    }
}
