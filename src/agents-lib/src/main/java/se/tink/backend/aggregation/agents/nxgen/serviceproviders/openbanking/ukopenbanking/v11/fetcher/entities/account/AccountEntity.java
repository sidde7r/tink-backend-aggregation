package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountEntity implements IdentifiableAccount {

    public static TransactionalAccount toTransactionalAccount(AccountEntity account, AccountBalanceEntity balance) {

        return TransactionalAccount
                .builder(account.getAccountType(),
                        account.getUniqueIdentifier(),
                        balance.getBalance())
                .setAccountNumber(account.getUniqueIdentifier())
                .setName(account.getNickname())
                .setBankIdentifier(account.getAccountId())
                .build();
    }

    public static CreditCardAccount toCreditCardAccount(AccountEntity account, AccountBalanceEntity balance) {

        return CreditCardAccount
                .builder(account.getUniqueIdentifier(),
                        balance.getBalance(),
                        balance.getAvaliableCredit()
                                .orElseThrow(() -> new IllegalStateException(
                                        "CreditCardAccount has no credit.")))
                .setAccountNumber(account.getUniqueIdentifier())
                .setBankIdentifier(account.getAccountId())
                .setName(account.getNickname())
                .build();
    }

    @JsonProperty("AccountId")
    private String accountId;
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("Nickname")
    private String nickname;
    @JsonProperty("Account")
    private AccountIdentifierEntity accountDetails;

    public String getAccountId() {
        return accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public AccountTypes getAccountType() {
        return AccountTypes.CHECKING;
    }

    public String getUniqueIdentifier() {
        return accountDetails.getIdentification();
    }

    @Override
    public String getBankIdentifier() {
        return accountId;
    }
}
