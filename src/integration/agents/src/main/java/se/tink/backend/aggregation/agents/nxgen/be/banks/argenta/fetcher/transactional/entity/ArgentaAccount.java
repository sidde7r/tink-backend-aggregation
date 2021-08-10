package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class ArgentaAccount {

    private String id;
    private String iban;
    private String alias;
    private String type;
    private String commercialName;
    private String shortCommercialName;
    private double balance;
    private String currency;

    public Optional<TransactionalAccount> toTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(getAccountName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(alias)
                .setBankIdentifier(id)
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return currency != null
                ? ExactCurrencyAmount.of(balance, currency)
                : ExactCurrencyAmount.of(balance, "EUR");
    }

    private TransactionalAccountType getAccountType() {
        return ArgentaConstants.ACCOUNT_TYPE_MAPPER.translate(type).orElse(null);
    }

    private String getAccountName() {
        return Optional.ofNullable(commercialName).orElse(shortCommercialName);
    }
}
