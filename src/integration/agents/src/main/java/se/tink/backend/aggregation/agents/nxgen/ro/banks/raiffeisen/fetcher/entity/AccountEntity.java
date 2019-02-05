package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

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

    public Amount toTinkAmount() {
        return new Amount(balances.get(0).getBalanceAmount().getCurrency(),
                balances.get(0).getBalanceAmount().getAmount()); // TODO: verify!
    }

    private String getAccountNumber() {
        if(!Strings.isNullOrEmpty(iban)) {
            return iban;
        }

        if(!Strings.isNullOrEmpty(bic)) {
            return bic;
        }

        return resourceId;
    }

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(AccountTypes.CHECKING, iban, toTinkAmount())
                .setName(accountName)
                .setAccountNumber(getAccountNumber())
                .putInTemporaryStorage(RaiffeisenConstants.STORAGE.TRANSACTIONS_URL, links.getTransactionUrl())
                .putInTemporaryStorage(RaiffeisenConstants.STORAGE.BALANCE_URL, links.getBalanceUrl())
                .putInTemporaryStorage(RaiffeisenConstants.STORAGE.ACCOUNT_ID, resourceId)
                .build();
    }
}
