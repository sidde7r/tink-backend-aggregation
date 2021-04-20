package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;

@JsonObject
@Slf4j
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountEntity {

    @Getter
    @JsonProperty("_id")
    private String id;

    @JsonProperty("_links")
    private List<LinkEntity> links;

    private String accountName;
    private List<AccountNumberEntity> accountNumbers;
    @Getter private String accountType;
    private BigDecimal availableBalance;
    private BankEntity bank;
    private String bookedBalance;
    private String country;
    private String creditLimit;
    private String currency;
    private String latestTransactionBookingDate;
    @Getter private String product;
    private String status;
    private String valueDatedBalance;

    @JsonIgnore
    public String getHolderName() {
        return formatHolderName();
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, getIban());
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        NordeaBaseConstants.ACCOUNT_TYPE_MAPPER,
                        accountType,
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getUniqueIdentifier())
                                .withAccountNumber(identifier.getIdentifier())
                                .withAccountName(
                                        Optional.ofNullable(product)
                                                .orElseThrow(
                                                        () ->
                                                                new NoSuchElementException(
                                                                        "Product is missing")))
                                .addIdentifier(identifier)
                                .build())
                .putInTemporaryStorage(NordeaBaseConstants.StorageKeys.ACCOUNT_ID, id)
                .setApiIdentifier(id)
                .build();
    }

    private String getUniqueIdentifier() {
        if (MarketCode.DK.name().equalsIgnoreCase(country)) {
            return extractAccountNumberFromIban();
        }
        return getIban();
    }

    private String extractAccountNumberFromIban() {
        return StringUtils.right(
                getIban(), NordeaBaseConstants.TransactionalAccounts.DANISH_ACCOUNT_NO_LENGTH);
    }

    @JsonIgnore
    public AccountIdentifier generalGetAccountIdentifier() {
        AccountIdentifier identifier = getAccountIdentifier();
        if (identifier.is(AccountIdentifierType.SE_NDA_SSN)) {
            return identifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        } else {
            return identifier;
        }
    }

    @JsonIgnore
    public AccountIdentifier getAccountIdentifier() {
        if (NordeaBaseConstants.TransactionalAccounts.PERSONAL_ACCOUNT.equalsIgnoreCase(product)) {
            AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(AccountIdentifierType.SE_NDA_SSN, getBban());
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier;
            }
        }
        return AccountIdentifier.create(AccountIdentifierType.SE, getBban());
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return new ExactCurrencyAmount(availableBalance, currency);
    }

    private String getBban() {
        return ListUtils.emptyIfNull(accountNumbers).stream()
                .filter(
                        acc ->
                                StringUtils.equalsIgnoreCase(
                                        acc.getType(),
                                        NordeaBaseConstants.AccountTypesResponse.BBAN_SE))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElse(getIban());
    }

    // Used by Nordea Sweden
    public String getLast4Bban() {
        return getBban().substring(getBban().length() - 4);
    }

    public String getIban() {
        return ListUtils.emptyIfNull(accountNumbers).stream()
                .filter(
                        acc ->
                                StringUtils.equalsIgnoreCase(
                                        acc.getType(),
                                        NordeaBaseConstants.AccountTypesResponse.IBAN))
                .findFirst()
                .map(AccountNumberEntity::getValue)
                .orElseThrow(
                        () -> {
                            log.info(
                                    "Failed to fetch iban "
                                            + LogTag.from("openbanking_base_nordea"));
                            return new IllegalArgumentException();
                        });
    }

    @JsonIgnore
    private String formatHolderName() {
        // We get accountName on the form "<SURNAME>,<FORENAME>", at least for Nordea Sweden.
        // Splitting on comma and changing the order of forename and surname.
        List<String> holderName =
                Stream.of(accountName.split(",")).map(String::trim).collect(Collectors.toList());
        return holderName.get(1) + " " + holderName.get(0);
    }
}
