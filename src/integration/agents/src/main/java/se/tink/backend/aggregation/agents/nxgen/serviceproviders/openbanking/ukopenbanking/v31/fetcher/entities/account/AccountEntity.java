package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
@Data
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

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Account")
    private List<AccountIdentifierEntity> identifiers;

    public static TransactionalAccount toTransactionalAccount(
            AccountEntity account, AccountBalanceEntity balance, String partyName) {

        String accountNumber = account.getUniqueIdentifier();
        String accountName = account.getDisplayName();
        String holder = account.getDefaultIdentifier().getOwnerName();

        HolderName holderName =
                Objects.nonNull(holder) ? new HolderName(holder) : new HolderName(partyName);

        /*
        TODO: We need to remove this ugly fix that has been done to make Revolut work without
        doing data migrations. uniqueIdentifier should always be accountNumber in ideal case.
         */

        Optional<String> revolutAccount =
                account.getIdentifiers().stream()
                        .filter(
                                e ->
                                        e.getIdentifierType()
                                                .equals(ExternalAccountIdentification4Code.IBAN))
                        .map(AccountIdentifierEntity::getIdentification)
                        .filter(x -> x.contains("REVO"))
                        .findAny();

        String uniqueIdentifier =
                revolutAccount.isPresent() ? account.getAccountId() : accountNumber;

        IdBuildStep idModuleBuilder =
                IdModule.builder()
                        .withUniqueIdentifier(uniqueIdentifier)
                        .withAccountNumber(accountNumber)
                        .withAccountName(accountName)
                        .addIdentifier(
                                account.toAccountIdentifier(accountName)
                                        .orElseThrow(
                                                () ->
                                                        new IllegalStateException(
                                                                "Unable to set identifier")));

        TransactionalAccount transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.from(account.getAccountType()).get())
                        .withoutFlags()
                        .withBalance(BalanceModule.of(balance.calculateAccountSpecificBalance()))
                        .withId(idModuleBuilder.build())
                        .setApiIdentifier(account.getAccountId())
                        .addHolderName(holderName.toString())
                        .build()
                        .get();

        return transactionalAccount;
    }

    public static CreditCardAccount toCreditCardAccount(
            AccountEntity account, AccountBalanceEntity balance, String partyName) {
        String holder = account.getDefaultIdentifier().getOwnerName();

        HolderName creditCardHolderName =
                Objects.nonNull(holder) ? new HolderName(holder) : new HolderName(partyName);

        return CreditCardAccount.builder(
                        account.getUniqueIdentifier(),
                        balance.calculateAccountSpecificBalance(),
                        balance.getAvailableCredit()
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "CreditCardAccount has no credit.")))
                .setAccountNumber(account.getUniqueIdentifier())
                .setBankIdentifier(account.getAccountId())
                .setName(account.getDisplayName())
                .setHolderName(creditCardHolderName)
                .build();
    }

    public String getAccountId() {
        return accountId;
    }

    public AccountIdentifierEntity getDefaultIdentifier() {
        return identifiers.stream()
                .filter(
                        e ->
                                e.getIdentifierType()
                                                .equals(
                                                        ExternalAccountIdentification4Code
                                                                .SORT_CODE_ACCOUNT_NUMBER)
                                        || e.getIdentifierType()
                                                .equals(ExternalAccountIdentification4Code.IBAN)
                                        || e.getIdentifierType()
                                                .equals(ExternalAccountIdentification4Code.PAN)
                                        || e.getIdentifierType()
                                                .equals(ExternalAccountIdentification4Code.BBAN)
                                        || e.getIdentifierType()
                                                .equals(
                                                        ExternalAccountIdentification4Code
                                                                .SAVINGS_ROLL_NUMBER))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Account details did not specify any SORT_CODE_ACCOUNT_NUMBER, BBAN, IBAN, PAN or SAVINGS_ROLL_NUMBER identifier."));
    }

    public String getUniqueIdentifier() {

        if (identifiers == null || identifiers.size() == 0) {
            throw new IllegalStateException("Account details did not specify any identifier.");
        }

        return getDefaultIdentifier().getIdentification();
    }

    public AccountTypes getAccountType() {
        return UkOpenBankingV31Constants.ACCOUNT_TYPE_MAPPER
                .translate(rawAccountSubType)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Unknown account types should have been filtered out before reaching this point!"));
    }

    public String getDisplayName() {
        if (nickname != null) {
            return nickname;
        }

        if (getDefaultIdentifier().getOwnerName() != null) {
            return getDefaultIdentifier().getOwnerName();
        }

        return getDefaultIdentifier().getIdentification();
    }

    public String getRawAccountSubType() {
        return rawAccountSubType;
    }

    public Optional<AccountIdentifier> toAccountIdentifier(String accountName) {
        return getDefaultIdentifier().toAccountIdentifier(accountName);
    }

    @Override
    public String getBankIdentifier() {
        return accountId;
    }
}
