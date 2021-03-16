package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_links")
    private List<LinkEntity> links;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("account_numbers")
    private List<AccountNumberEntity> accountNumbers;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;

    private BankEntity bank;

    @JsonProperty("booked_balance")
    private String bookedBalance;

    private String country;

    @JsonProperty("credit_limit")
    private String creditLimit;

    private String currency;

    @JsonProperty("latest_transaction_booking_date")
    private String latestTransactionBookingDate;

    private String product;

    private String status;

    @JsonProperty("value_dated_balance")
    private String valueDatedBalance;

    public String getId() {
        return id;
    }

    public List<LinkEntity> getLinks() {
        return links;
    }

    public String getAccountName() {
        return accountName;
    }

    public List<AccountNumberEntity> getAccountNumbers() {
        return accountNumbers;
    }

    public String getAccountType() {
        return accountType;
    }

    public BankEntity getBank() {
        return bank;
    }

    public String getBookedBalance() {
        return bookedBalance;
    }

    public String getCountry() {
        return country;
    }

    public String getCreditLimit() {
        return creditLimit;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLatestTransactionBookingDate() {
        return latestTransactionBookingDate;
    }

    public String getProduct() {
        return product;
    }

    public String getStatus() {
        return status;
    }

    public String getValueDatedBalance() {
        return valueDatedBalance;
    }

    @JsonIgnore
    public String getHolderName() {
        return formatHolderName();
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, getIban());
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        NordeaBaseConstants.ACCOUNT_TYPE_MAPPER,
                        accountType,
                        TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getIban())
                                .withAccountNumber(identifier.getIdentifier())
                                .withAccountName(Optional.ofNullable(accountName).orElse(product))
                                .addIdentifier(identifier)
                                .build())
                .putInTemporaryStorage(NordeaBaseConstants.StorageKeys.ACCOUNT_ID, id)
                .setApiIdentifier(id)
                .build();
    }

    @JsonIgnore
    public AccountIdentifier generalGetAccountIdentifier() {
        AccountIdentifier identifier = getAccountIdentifier();
        if (identifier.is(AccountIdentifier.Type.SE_NDA_SSN)) {
            return identifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        } else {
            return identifier;
        }
    }

    @JsonIgnore
    public AccountIdentifier getAccountIdentifier() {
        if (NordeaBaseConstants.TransactionalAccounts.PERSONAL_ACCOUNT.equalsIgnoreCase(product)) {
            AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(AccountIdentifier.Type.SE_NDA_SSN, getBban());
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier;
            }
        }
        return AccountIdentifier.create(AccountIdentifier.Type.SE, getBban());
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
                            logger.info(
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
