package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.deserializer.AccountIdentifierDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountEntity {

    @JsonProperty("AccountId")
    private String accountId;
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("Nickname")
    private String nickname;
    @JsonProperty("AccountType")
    private String rawAccountType;
    @JsonProperty("AccountSubType")
    private String rawAccountSubType;
    @JsonProperty("Account")
    @JsonDeserialize(using = AccountIdentifierDeserializer.class)
    private Map<String, AccountIdentifierEntity> accountDetails;

    public String getAccountId() {
        return accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRawAccountType() {
        return rawAccountType;
    }

    public String getRawAccountSubType() {
        return rawAccountSubType;
    }

    public String getUniqueIdentifier() {

        return UkOpenBankingV20Constants.ACCOUNT_IDENTIFIERS.stream()
                .filter(accountNumber -> accountDetails.containsKey(accountNumber))
                .findFirst()
                .map(accountNumber -> accountDetails.get(accountNumber).getIdentification())
                .filter(id -> !Strings.isNullOrEmpty(id))
                .orElseThrow(
                        () -> new IllegalStateException("Account details did not specify a recognized identifier.")
                );
    }

    public AccountTypes getAccountType() {

        return UkOpenBankingV20Constants.AccountTypeTranslator.translate(rawAccountSubType)
                .orElse(AccountTypes.OTHER);
    }

    public static TransactionalAccount toTransactionalAccount(AccountEntity account, AccountBalanceEntity balance) {

        return TransactionalAccount
                .builder(AccountTypes.CHECKING,
                        account.getUniqueIdentifier(),
                        balance.getBalance())
                .setAccountNumber(account.getUniqueIdentifier())
                .setBankIdentifier(account.getAccountId())
                .setName(account.getNickname())
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
}
