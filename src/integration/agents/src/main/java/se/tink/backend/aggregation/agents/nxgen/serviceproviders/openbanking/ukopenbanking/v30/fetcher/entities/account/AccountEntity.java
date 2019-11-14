package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.UkOpenBankingV30Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@JsonObject
public class AccountEntity implements IdentifiableAccount {
    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("AccountType")
    private String rawAccountType;

    @JsonProperty("AccountSubType")
    private String rawAccountSubType;

    @JsonProperty("Nickname")
    private String nickname;

    @JsonProperty("Account")
    private AccountIdentifierEntity identifierEntity;

    public static TransactionalAccount toTransactionalAccount(
            AccountEntity account, AccountBalanceEntity balance) {
        String accountNumber = account.getUniqueIdentifier();
        String accountName = account.getDisplayName();

        IdBuildStep idModuleBuilder =
                IdModule.builder()
                        .withUniqueIdentifier(accountNumber)
                        .withAccountNumber(accountNumber)
                        .withAccountName(accountName)
                        .addIdentifier(new SortCodeIdentifier(accountNumber));

        if (account.toAccountIdentifier(accountName).isPresent()) {
            idModuleBuilder.addIdentifier(account.toAccountIdentifier(accountName).get());
        }

        TransactionalAccount transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.from(account.getAccountType()).get())
                        .withoutFlags()
                        .withBalance(BalanceModule.of(balance.getBalance()))
                        .withId(idModuleBuilder.build())
                        .setApiIdentifier(account.getAccountId())
                        .build()
                        .get();

        return transactionalAccount;
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

    public String getAccountId() {
        return accountId;
    }

    public String getUniqueIdentifier() {

        if (identifierEntity == null) {
            throw new IllegalStateException("Account details did not specify an identifier.");
        }

        return identifierEntity.getIdentification();
    }

    public AccountTypes getAccountType() {
        return UkOpenBankingV30Constants.ACCOUNT_TYPE_MAPPER
                .translate(rawAccountSubType)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Unknown account types should have been filtered out before reaching this point!"));
    }

    public String getDisplayName() {
        return nickname != null ? nickname : identifierEntity.getName();
    }

    public String getRawAccountSubType() {
        return rawAccountSubType;
    }

    private Optional<AccountIdentifier> toAccountIdentifier(String accountName) {
        return identifierEntity.toAccountIdentifier(accountName);
    }

    @Override
    public String getBankIdentifier() {
        return accountId;
    }
}
