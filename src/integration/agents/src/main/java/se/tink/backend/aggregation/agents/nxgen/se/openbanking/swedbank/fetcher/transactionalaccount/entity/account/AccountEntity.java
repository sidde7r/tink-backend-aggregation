package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity {
    private String id;
    private String iban;
    private String accountType;
    private String currency;
    private String cashAccountType;
    private String bic;
    private String bankId;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public String getIban() {
        return iban;
    }

    public String getId() {
        return id;
    }

    public TransactionalAccount toTinkAccount(AccountBalanceResponse accountBalanceResponse) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(id)
                .setBalance(accountBalanceResponse.getAvailableBalance(currency))
                .setAlias(id)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .putInTemporaryStorage(SwedbankConstants.StorageKeys.ACCOUNT_ID, iban)
                .setApiIdentifier(id)
                .build();
    }
}
