package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity implements IdentifiableAccount {

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Nickname")
    private String nickname;

    @JsonProperty("Account")
    private AccountIdentifierEntity identifierEntity;

    public String getAccountId() {
        return accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountTypes getAccountType() {
        return AccountTypes.CHECKING;
    }

    public String getUniqueIdentifier() {

        // In order to avoid account duplication we throw error if account does not have sort-code
        // identifier.
        if (!identifierEntity.isSortCode()) {
            throw new IllegalStateException("Sort-code identifier needed for unique identifier.");
        }

        return identifierEntity.getIdentification();
    }

    public String getDisplayName() {
        return nickname != null ? nickname : identifierEntity.getName();
    }

    private AccountIdentifier toAccountIdentifier(String accountName) {
        return identifierEntity.toAccountIdentifier(accountName);
    }

    @Override
    public String getBankIdentifier() {
        return accountId;
    }

    public static TransactionalAccount toTransactionalAccount(
            AccountEntity account, AccountBalanceEntity balance) {
        String accountNumber = account.getUniqueIdentifier();
        String accountName = account.getDisplayName();

        return TransactionalAccount.builder(
                        account.getAccountType(), accountNumber, balance.getBalance())
                .setAccountNumber(accountNumber)
                .setName(accountName)
                .addIdentifier(account.toAccountIdentifier(accountName))
                .setBankIdentifier(account.getAccountId())
                .build();
    }

    public static CreditCardAccount toCreditCardAccount(
            AccountEntity account, AccountBalanceEntity balance) {

        return CreditCardAccount.builder(
                        account.getUniqueIdentifier(),
                        balance.getBalance(),
                        balance.getAvailableCredit()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "CreditCardAccount has no credit.")))
                .setAccountNumber(account.getUniqueIdentifier())
                .setBankIdentifier(account.getAccountId())
                .setName(account.getDisplayName())
                .build();
    }
}
