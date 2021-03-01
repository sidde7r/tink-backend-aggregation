package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Currency;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities.HolderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@Getter
@JsonObject
@Slf4j
public class BankAccountsEntity {
    @JsonProperty("Balance")
    private BalanceEntity balance;

    @JsonProperty("Currency")
    private int currency;

    @JsonProperty("DisplayTypeName")
    private String displayTypeName = "";

    @JsonProperty("DisplayNumber")
    private String displayNumber = "";

    @JsonProperty("EncryptedNumber")
    private String encryptedNumber = "";

    @JsonProperty("Holder")
    private HolderEntity holder;

    @JsonProperty("Interest")
    private BigDecimal interest;

    @JsonProperty("LendingInterest")
    private BigDecimal lendingInterest;

    @JsonProperty("OwnedBySelf")
    private boolean ownedBySelf;

    @JsonProperty("Position")
    private int position;

    @JsonProperty("RoleType")
    private int roleType;

    @JsonProperty("RoleTypeName")
    private String roleTypeName = "";

    @JsonProperty("Status")
    private int status;

    @JsonProperty("StatusName")
    private String statusName = "";

    @JsonProperty("Type")
    private int type;

    @JsonProperty("Reference")
    private String reference = "";

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("Name")
    private String name = "";

    @JsonProperty("Number")
    private String number = "";

    @JsonProperty("TypeName")
    private String typeName;

    @JsonIgnore
    private static final List<TransactionalAccountType> TRANSACTIONAL_ACCOUNT_TYPES =
            ImmutableList.of(TransactionalAccountType.CHECKING, TransactionalAccountType.SAVINGS);

    @JsonIgnore
    private String getDisplayName() {
        return Optional.ofNullable(displayName).orElse(displayTypeName);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return SkandiaBankenConstants.ACCOUNT_TYPE_MAPPER.isOneOf(
                typeName, TRANSACTIONAL_ACCOUNT_TYPES);
    }

    @JsonIgnore
    public boolean isCreditCardAccount() {
        return SkandiaBankenConstants.AccountType.CREDITCARD.equalsIgnoreCase(typeName);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkTransactionalAccount() {

        Currency accountCurrency = Currency.fromCode(currency);

        if (accountCurrency == null) {
            log.warn("Unknown currency code {} - omitting account", currency);
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(SkandiaBankenConstants.ACCOUNT_TYPE_MAPPER, typeName)
                .withBalance(BalanceModule.of(balance.getAmount(accountCurrency.toString())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(number)
                                .withAccountNumber(number)
                                .withAccountName(getDisplayName())
                                .addIdentifier(new SwedishIdentifier(number))
                                .addIdentifier(new SwedishIdentifier(number).toIbanIdentifer())
                                .build())
                .addHolderName(holder.getHolderName())
                .setApiIdentifier(encryptedNumber)
                .setBankIdentifier(encryptedNumber)
                .build();
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount(CardEntity cardEntity) {
        CreditCardModule cardModule =
                CreditCardModule.builder()
                        .withCardNumber(cardEntity.getDisplayName())
                        .withBalance(
                                balance.getAmount(SkandiaBankenConstants.Currency.SEK.toString()))
                        .withAvailableCredit(
                                balance.getDisposableAmount(
                                        SkandiaBankenConstants.Currency.SEK.toString()))
                        .withCardAlias(cardEntity.getDisplayName())
                        .build();

        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(number)
                        .withAccountName(getDisplayName())
                        .addIdentifier(new SwedishIdentifier(number))
                        .build();

        return CreditCardAccount.nxBuilder()
                .withCardDetails(cardModule)
                .withInferredAccountFlags()
                .withId(idModule)
                .addHolderName(holder.getHolderName())
                .setApiIdentifier(encryptedNumber)
                .build();
    }
}
