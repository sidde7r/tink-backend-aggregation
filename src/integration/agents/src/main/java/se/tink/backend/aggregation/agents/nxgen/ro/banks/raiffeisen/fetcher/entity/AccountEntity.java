package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
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

    @JsonProperty("name")
    private String accountName;

    private String product;
    private String cashAccountType;
    private String bic;
    private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(
                balances.get(0).getBalanceAmount().getAmount(),
                balances.get(0).getBalanceAmount().getCurrency());
    }

    private String getAccountNumber() {
        if (!Strings.isNullOrEmpty(iban)) {
            return iban;
        }

        if (!Strings.isNullOrEmpty(bic)) {
            return bic;
        }

        return resourceId;
    }

    public Optional<TransactionalAccount> toTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(BalanceModule.of(toTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(accountName)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .putInTemporaryStorage(
                        RaiffeisenConstants.STORAGE.TRANSACTIONS_URL, links.getTransactionUrl())
                .putInTemporaryStorage(
                        RaiffeisenConstants.STORAGE.BALANCE_URL, links.getBalanceUrl())
                .putInTemporaryStorage(RaiffeisenConstants.STORAGE.ACCOUNT_ID, resourceId)
                .build();
    }
}
