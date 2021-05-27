package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class SavingInvestmentEntity {
    private String associatedAccount;
    private String policyAccount;
    private String depositId;
    private String description;
    private BigDecimal amount;
    private String product;

    public Optional<TransactionalAccount> toTinkTransactionalAccount(String currency) {
        final AccountIdentifier ibanIdentifier =
                AccountIdentifier.create(
                        AccountIdentifierType.OTHER, translateAccountType(), description);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(toTinkAmountBalance(currency))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(associatedAccount)
                                .withAccountNumber(associatedAccount)
                                .withAccountName(description)
                                .addIdentifier(ibanIdentifier)
                                .build())
                .setApiIdentifier(depositId)
                .build();
    }

    @JsonIgnore
    private BalanceModule toTinkAmountBalance(String currency) {
        return BalanceModule.of(ExactCurrencyAmount.of(amount, currency));
    }

    @JsonIgnore
    private String translateAccountType() {
        return String.valueOf(policyAccount);
    }
}
