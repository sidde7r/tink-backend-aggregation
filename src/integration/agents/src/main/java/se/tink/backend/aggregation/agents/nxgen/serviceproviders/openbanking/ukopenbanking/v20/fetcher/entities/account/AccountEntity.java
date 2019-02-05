package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.deserializer.AccountIdentifierDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity implements IdentifiableAccount {
    private static AggregationLogger log = new AggregationLogger(AccountEntity.class);

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
    private Map<UkOpenBankingV20Constants.AccountIdentifier, AccountIdentifierEntity> accountIdentifierMap;

    public String getAccountId() {
        return accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getUniqueIdentifier() {
        return getIdentifierEntity().getIdentification();
    }

    public AccountTypes getAccountType() {
        return UkOpenBankingV20Constants.ACCOUNT_TYPE_MAPPER.translate(rawAccountSubType)
                .orElseThrow(() -> new IllegalStateException("Unknown account types should have been filtered out before reaching this point!"));
    }

    public String getDisplayName() {
        return nickname != null ? nickname : getIdentifierEntity().getName();
    }

    private AccountIdentifierEntity getIdentifierEntity() {
        return UkOpenBankingV20Constants.AccountIdentifier
                .getPreferredIdentifierType(accountIdentifierMap.keySet())
                .map(accountIdentifierMap::get)
                .orElseThrow(
                        () -> new IllegalStateException("Account details did not specify a recognized identifier.")
                );
    }

    private Optional<AccountIdentifier> toAccountIdentifier(String accountName) {
        return getIdentifierEntity().toAccountIdentifier(accountName);
    }

    public String getRawAccountSubType() {
        return rawAccountSubType;
    }

    public static TransactionalAccount toTransactionalAccount(AccountEntity account, AccountBalanceEntity balance) {
        String accountNumber = account.getUniqueIdentifier();
        String accountName = account.getDisplayName();

        TransactionalAccount.Builder accountBuilder = TransactionalAccount
                .builder(account.getAccountType(),
                        accountNumber,
                        balance.getBalance())
                .setAccountNumber(accountNumber)
                .setName(accountName)
                .setBankIdentifier(account.getAccountId());

        account.toAccountIdentifier(accountName).ifPresent(accountBuilder::addIdentifier);

        return accountBuilder.build();
    }

    public static CreditCardAccount toCreditCardAccount(AccountEntity account, AccountBalanceEntity balance) {

        log.info("Found UKOB credit card!");
        return CreditCardAccount
                .builder(account.getUniqueIdentifier(),
                        balance.getBalance(),
                        balance.getAvailableCredit()
                                .orElseThrow(() -> new IllegalStateException(
                                        "CreditCardAccount has no credit.")))
                .setAccountNumber(account.getUniqueIdentifier())
                .setBankIdentifier(account.getAccountId())
                .setName(account.getDisplayName())
                .build();
    }

    @Override
    public String getBankIdentifier() {
        return accountId;
    }
}
